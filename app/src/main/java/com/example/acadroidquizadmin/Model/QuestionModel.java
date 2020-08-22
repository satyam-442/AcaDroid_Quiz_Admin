package com.example.acadroidquizadmin.Model;

public class QuestionModel {
    public String id, question, optiona, optionb, optionc, optiond, correctAns, setId;

    public QuestionModel() {
    }

    public QuestionModel(String id, String question, String optiona, String optionb, String optionc, String optiond, String correctAns, String setId) {
        this.id = id;
        this.question = question;
        this.optiona = optiona;
        this.optionb = optionb;
        this.optionc = optionc;
        this.optiond = optiond;
        this.correctAns = correctAns;
        this.setId = setId;
    }

    public String getIdd() {
        return id;
    }

    public void setIdd(String id) {
        this.id = id;
    }

    public String getQuestionn() {
        return question;
    }

    public void setQuestionn(String question) {
        this.question = question;
    }

    public String getOptionaa() {
        return optiona;
    }

    public void setOptionaa(String optiona) {
        this.optiona = optiona;
    }

    public String getOptionbb() {
        return optionb;
    }

    public void setOptionbb(String optionb) {
        this.optionb = optionb;
    }

    public String getOptioncc() {
        return optionc;
    }

    public void setOptioncc(String optionc) {
        this.optionc = optionc;
    }

    public String getOptiondd() {
        return optiond;
    }

    public void setOptiondd(String optiond) {
        this.optiond = optiond;
    }

    public String getCorrectAnss() {
        return correctAns;
    }

    public void setCorrectAnss(String correctAns) {
        this.correctAns = correctAns;
    }

    public String getSetIdd() {
        return setId;
    }

    public void setSetIdd(String setId) {
        this.setId = setId;
    }
}
