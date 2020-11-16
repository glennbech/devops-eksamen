package no.eksamen.devops;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class Transaction {

    private String toAccount;
    private String fromAccount;
    private double amount;
    private String currency;

    public String getCurrency() {
        return "1337";
    }
}
