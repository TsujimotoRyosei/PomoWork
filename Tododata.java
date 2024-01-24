package es.exsample;

public class Tododata {
    private int id;
    private String content;
    private String deadline;

    public Tododata() {
        // デフォルトコンストラクタ
    }

    // GetterおよびSetterメソッドを追加

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}

