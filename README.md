This is the miniproject repo for 4156 in Fall 2026.

## Static Analysis (PMD)

I used PMD to check the Java code for possible bugs and other problems.

### Install

Download and unzip PMD:

```bash
curl -L -o pmd.zip https://github.com/pmd/pmd/releases/download/pmd_releases%2F7.x.x/pmd-dist-7.x.x-bin.zip
unzip pmd.zip
```

The `7.x.x` part should be replaced with the PMD version being used.

### Run

From the project folder, run:

```bash
./pmd-bin-7.x.x/bin/pmd check -d src/main/java -R rulesets/java/quickstart.xml -f text
```

PMD will print any issues it finds, including the file, line number, and rule that was triggered.

### Reviewing the results

I looked through the PMD results and checked the reported issues against the code and Javadoc. I focused on issues that could actually affect the program's behavior, instead of just style warnings.

Any bugs I found were fixed and documented in `bugs.txt`.

### Coverage notes

* `com.taxapi` has 0% coverage because it only contains the Spring Boot main class, which does not need to be tested.
* `GcsStorageService` is not included in JaCoCo coverage because testing it requires a live GCP setup. This is planned for Assignment 3.
