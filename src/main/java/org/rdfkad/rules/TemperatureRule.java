package org.rdfkad.rules;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;

@Rule(name = "Temperature rule", description = "Trigger an alarm if the temperature is too high")
public class TemperatureRule {

    @Condition
    public boolean whenHigh(@Fact("temperature") int temperature) {
        return temperature > 45; // Condition for triggering the high temperature rule
    }

    @Action
    public void thenHigh(@Fact("temperature") int temperature) {
        System.out.println("High temperature alarm triggered! Current value: " + temperature);
    }

    @Condition
    public boolean whenLow(@Fact("temperature") int temperature) {
        return temperature <= 45; // Condition for triggering the low temperature rule
    }

    @Action
    public void thenLow(@Fact("temperature") int temperature) {
        System.out.println("Low temperature alarm triggered! Current value: " + temperature);
    }
}