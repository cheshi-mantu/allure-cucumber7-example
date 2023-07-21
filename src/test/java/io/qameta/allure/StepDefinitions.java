package io.qameta.allure;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.junit.jupiter.api.Assertions.*;

public class StepDefinitions {

    @When("today is Sunday")
    public void todayIsSunday() {
    }

    @Given("I ask whether it's Friday yet")
    public void askWhetherItsFridayYet() {
    }

    @Then("I should be told {string}")
    public void shouldBeTold(String text) {
    }

}
