package me.meenagopal24.recyclerdemo;

public class SqlResponse {
    class result{

        String fieldCount;
        String insertId;

        public result(String fieldCount, String insertId, String fileName) {
            this.fieldCount = fieldCount;
            this.insertId = insertId;
            this.fileName = fileName;
        }

        String fileName;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public String toString() {
            return "result{" +
                    "fieldCount='" + fieldCount + '\'' +
                    ", insertId='" + insertId + '\'' +
                    '}';
        }

        public String getFieldCount() {
            return fieldCount;
        }

        public void setFieldCount(String fieldCount) {
            this.fieldCount = fieldCount;
        }

        public String getInsertId() {
            return insertId;
        }

        public void setInsertId(String insertId) {
            this.insertId = insertId;
        }

        public result(String fieldCount, String insertId) {
            this.fieldCount = fieldCount;
            this.insertId = insertId;
        }
    }
    String code;
    result result;

    public SqlResponse(String code, SqlResponse.result result) {
        this.code = code;
        this.result = result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public SqlResponse.result getResult() {
        return result;
    }

    public void setResult(SqlResponse.result result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "SqlResponse{" +
                "code='" + code + '\'' +
                ", result=" + result +
                '}';
    }
}
