package model;

import java.util.ArrayList;

public class ResultTransaction {

    private int id;
    private String code;
    private String result;
    private String message;
    private ArrayList<String> arrayResult;

    public ResultTransaction() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<String> getArrayResult() {
        return arrayResult;
    }

    public void setArrayResult(ArrayList<String> arrayResult) {
        this.arrayResult = arrayResult;
    }

    public String toStringArrayResult() {
        String errorResult = "";
        if (arrayResult != null) {
            for (int i = 0; i < arrayResult.size(); i++) {
                errorResult += (i + 1) + ": " + arrayResult.get(i) + "\n";
            }
        }
        return errorResult;
    }

}
