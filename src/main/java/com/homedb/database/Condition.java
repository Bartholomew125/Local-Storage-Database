package com.homedb.database;

public class Condition {

    private String condition;

    public Condition(String condition) {
        this.condition = condition;
    }

    public String getCondition() {
        return this.condition;
    }

    public static class Where {

        public static Condition equals(String column1, String column2) {
            return new Condition(column1 + " = " + column2);
        }

    }
}
