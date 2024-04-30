package logger;

public enum ErrorLogMessage {
    NAME_NOT_VALID("Name not valid"),
    MAIL_ALREADY_TAKEN("Mail already taken"),
    MAIL_NOT_VALID("Mail not valid"),
    COMBINATION_MAIL_PWD_INVALID("Combination mail and password is invalid"),
    NOT_RESPONDING_TO_REQUEST("Server is not responding to request"),
    NO_SALES_IN_THAT_DOMAIN("There is no annonce in that domain"),
    NOT_OWNER("You are not the owner of that annonce");

    private String content;

    ErrorLogMessage(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
