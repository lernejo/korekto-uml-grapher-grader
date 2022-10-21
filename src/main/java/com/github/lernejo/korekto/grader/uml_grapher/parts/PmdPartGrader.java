package com.github.lernejo.korekto.grader.uml_grapher.parts;

import com.github.lernejo.korekto.grader.uml_grapher.LaunchingContext;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.PartGrader;
import com.github.lernejo.korekto.toolkit.thirdparty.pmd.FileReport;
import com.github.lernejo.korekto.toolkit.thirdparty.pmd.PmdExecutor;
import com.github.lernejo.korekto.toolkit.thirdparty.pmd.Rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This is a copy from korekto-toolkit as JitPack is down and this needs to be fixed.
 * To remove after upgrading to KTK 1.3.6.
 */
public record PmdPartGrader(String name, double minGrade, Rule... rules) implements PartGrader<LaunchingContext> {
    @Override
    public GradePart grade(LaunchingContext context) {
        var pmdReports = new PmdExecutor().runPmd(
            context.getExercise(),
            rules
        );
        var ruleTolerances = new HashMap<String, Integer>();
        Arrays.stream(rules).filter(it -> it.getExceptions() > 0).forEach(it -> ruleTolerances.put(it.getName(), it.getExceptions()));

        if (pmdReports.isEmpty()) {
            return new GradePart(name(), 0.0, null, List.of("No analysis can be performed"));
        }

        var sortedReports = pmdReports.stream().sorted(Comparator.comparing(r -> r.getFileReports().size())).toList();

        var violations = new AtomicInteger();
        var messages = new ArrayList<String>();

        for (var pmdReport : sortedReports) {
            var fileReports = pmdReport.getFileReports().stream().sorted(Comparator.comparing(fr -> fr.getName())).toList();

            fileReports
                .stream()
                .map(
                    (FileReport fr) ->
                        buildViolationsBlock(
                            ruleTolerances,
                            fr
                        )
                )
                .filter(
                    it -> it.violations() > 0
                )
                .map(it -> {
                    violations.addAndGet(it.violations);
                    return it.fileReportName + it.report;
                })
                .forEach(messages::add);
        }

        if (messages.isEmpty()) {
            messages.add("OK");
        }
        return new GradePart(name(), Math.max(violations.get() * minGrade() / 4, minGrade()), null, messages);
    }

    private ViolationBlock buildViolationsBlock(Map<String, Integer> ruleTolerances, FileReport fileReport) {
        var violations = fileReport
            .getViolations()
            .stream()
            .filter(it -> {
                if (ruleTolerances.containsKey(it.getRule())) {
                    ruleTolerances.compute(it.getRule(), (k, v) -> v - 1);
                }
                return !ruleTolerances.containsKey(it.getRule()) || ruleTolerances.get(it.getRule()) < 0;
            })
            .toList();
        var report = violations
            .stream()
            .sorted(Comparator.comparing(it -> it.getBeginLine() * 10000 + it.getBeginColumn()))
            .map(v -> "L." + v.getBeginLine() + ": " + v.getMessage().trim())
            .collect(Collectors.joining("\n            * ", "\n            * ", ""));
        return new ViolationBlock(fileReport.getName(), report, violations.size());
    }

    record ViolationBlock(String fileReportName, String report, int violations) {
    }
}
