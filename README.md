# allure-cucumber7-example
allure-cucumber7-example

### Tests selective run (filtering)

This feature works when you are triggering a build from Allure TestOps by selecting seperate test cases (not all in your project).

Allure TestOps agent creates **testplan.json** file  with the list of tests on CI, then the test framework adaptor reads the content and makes the seletion of the tests to be executed.

#### Requirements

Allure framework libraries 2.13.9+

#### How to check locally

For this particular exapmple we have **testplan.json** with the following content:

```JSON
{
  "version": "1.0",
  "tests": [
    {
      "id": "12",
      "selector": "Is it Friday yet?: Sunday isn't Friday"
    }
  ]
}
```
So, we expect only `"Is it Friday yet?: Sunday isn't Friday"` test to be executed.

##### Steps

In the terminal type the following:

```bash
export ALLURE_TESTPLAN_PATH=testplan.json
# just to check the variable was created 
echo $ALLURE_TESTPLAN_PATH
./mvnw clean test
```
