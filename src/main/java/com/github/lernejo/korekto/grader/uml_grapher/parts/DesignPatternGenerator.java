package com.github.lernejo.korekto.grader.uml_grapher.parts;

import com.github.lernejo.korekto.grader.uml_grapher.LaunchingContext;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Relation;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Relationship;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.TargetType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static com.github.lernejo.korekto.grader.uml_grapher.parts.ByteBuddys.*;

public class DesignPatternGenerator {

    public static TypesSupplier.Types generateSingleton(ClassLoader cl, LaunchingContext c) {
        Path directory = ByteBuddys.createTempClassDirectory(c.randomSupplier());
        Class<?> singleton = saveToFileAndLoad(
            BYTE_BUDDY
                .subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                .name(GEN_PACKAGE + "Singleton_" + c.newTypeId())
                .defineField("INSTANCE", TargetType.class, Visibility.PRIVATE, Ownership.STATIC)
                .defineConstructor(Visibility.PRIVATE)
                .intercept(MethodCall
                    .invoke(ReflectUtils.getConstructor(Object.class))
                    .onSuper())
                .defineMethod("getInstance", TargetType.class, Visibility.PUBLIC, Ownership.STATIC)
                .intercept(FixedValue.nullValue())
                .defineMethod("getConfiguration", String.class, Visibility.PUBLIC)
                .intercept(FixedValue.value("the-configuration"))
                .make(),
            directory,
            cl);
        String className = singleton.getSimpleName();
        Class<?>[] classes = new Class<?>[]{singleton};
        Set<Relation> relations = Set.of(new Relation(className, Relationship.ASSOCIATES_WITH, className));
        return new TypesSupplier.Types(classes, classes, relations, directory);
    }

    public static TypesSupplier.Types generateProxy(ClassLoader cl, LaunchingContext c) {
        Path directory = ByteBuddys.createTempClassDirectory(c.randomSupplier());
        Set<Relation> relations = new HashSet<>();
        Class<?> image = saveToFileAndLoad(
            BYTE_BUDDY
                .makeInterface()
                .name(GEN_PACKAGE + "Image_" + c.newTypeId())
                .defineMethod("display", void.class, Modifier.PUBLIC)
                .withoutCode()
                .make()
            ,
            directory, cl);

        Class<?> realImage = saveToFileAndLoad(
            BYTE_BUDDY
                .subclass(Object.class)
                .name(GEN_PACKAGE + "RealImage_" + c.newTypeId())
                .implement(image)
                .defineField("fileName", String.class, Visibility.PRIVATE, Ownership.MEMBER)
                .defineMethod("display", void.class, Modifier.PUBLIC)
                .intercept(MethodDelegation.toConstructor(String.class))
                .defineMethod("loadFromDisk", void.class, Visibility.PRIVATE, Ownership.MEMBER)
                .withParameters(String.class)
                .intercept(MethodDelegation.toConstructor(String.class))
                .make()
            ,
            directory, cl);
        relations.add(new Relation(realImage.getSimpleName(), Relationship.REALIZES, image.getSimpleName()));

        Class<?> lazyProxyImage = saveToFileAndLoad(
            BYTE_BUDDY
                .subclass(Object.class)
                .name(GEN_PACKAGE + "LazyImage_" + c.newTypeId())
                .implement(image)
                .defineField("fileName", String.class, Visibility.PRIVATE, Ownership.MEMBER)
                .defineField("delegate", realImage, Visibility.PRIVATE, Ownership.MEMBER)
                .defineMethod("display", void.class, Modifier.PUBLIC)
                .intercept(MethodDelegation.toConstructor(String.class))
                .make()
            ,
            directory, cl);
        relations.add(new Relation(lazyProxyImage.getSimpleName(), Relationship.REALIZES, image.getSimpleName()));
        relations.add(new Relation(lazyProxyImage.getSimpleName(), Relationship.ASSOCIATES_WITH, realImage.getSimpleName()));

        Class<?>[] allTypes = new Class<?>[]{image, realImage, lazyProxyImage};
        Class<?>[] selectedTypes = new Class<?>[]{image};


        return new TypesSupplier.Types(allTypes, selectedTypes, relations, directory);
    }
}
