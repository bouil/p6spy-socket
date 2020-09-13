package net.bouillon.p6spy.socket;

public class SqlLog {

    private int connectionId;
    private String now;
    private long elapsed;
    private String category;
    private String prepared;
    private String sql;
    private String url;

    public SqlLog(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        this.connectionId = connectionId;
        this.now = now;
        this.elapsed = elapsed;
        this.category = category;
        this.prepared = prepared;
        this.sql = sql;
        this.url = url;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public String getNow() {
        return now;
    }

    public void setNow(String now) {
        this.now = now;
    }

    public long getElapsed() {
        return elapsed;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrepared() {
        return prepared;
    }

    public void setPrepared(String prepared) {
        this.prepared = prepared;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}