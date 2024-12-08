# korekto-uml-grapher-grader
Grader for the UML grapher project

[![Build](https://github.com/lernejo/korekto-uml-grapher-grader/actions/workflows/ci.yml/badge.svg)](https://github.com/lernejo/korekto-uml-grapher-grader/actions)
[![codecov](https://codecov.io/gh/lernejo/korekto-uml-grapher-grader/branch/main/graph/badge.svg)](https://codecov.io/gh/lernejo/korekto-uml-grapher-grader)
![License](https://img.shields.io/badge/License-Elastic_License_v2-blue)

Exercise subject: [here](EXERCISE_fr.adoc)

# How to launch
You will need these 2 env vars:
* `GH_LOGIN` your GitHub login
* `GH_TOKEN` a [**P**ersonal **A**ccess **T**oken](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic) with permissions:
    * (classic) : `repo` and `user`
    * (fine-grained):
        * Repository permissions:
            * **Actions**: `Read-only`
            * **Contents**: `Read-only`

```bash
git clone git@github.com:lernejo/korekto-uml-grapher-grader.git
cd korekto-uml-grapher-grader
./mvnw compile exec:exec -Dgithub.login="$GH_LOGIN" -Dgithub.token="$GH_TOKEN"
```
