package com.lanrhyme.shardlauncher.utils.file;

public class InvalidFilenameException extends RuntimeException {
    private final FilenameErrorType type;
    private String illegalCharacters = null;
    private int invalidLength = -1;

    public InvalidFilenameException(String message, String illegalCharacters) {
        super(message);
        this.type = FilenameErrorType.CONTAINS_ILLEGAL_CHARACTERS;
        this.illegalCharacters = illegalCharacters;
    }

    public InvalidFilenameException(String message, int invalidLength) {
        super(message);
        this.type = FilenameErrorType.INVALID_LENGTH;
        this.invalidLength = invalidLength;
    }

    public InvalidFilenameException(String message, @SuppressWarnings("unused") boolean isLeadingOrTrailingSpace) {
        super(message);
        this.type = FilenameErrorType.LEADING_OR_TRAILING_SPACE;
    }

    public boolean containsIllegalCharacters() {
        return type == FilenameErrorType.CONTAINS_ILLEGAL_CHARACTERS;
    }

    public String getIllegalCharacters() {
        return illegalCharacters;
    }

    public boolean isInvalidLength() {
        return type == FilenameErrorType.INVALID_LENGTH;
    }

    public int getInvalidLength() {
        return invalidLength;
    }

    public boolean isLeadingOrTrailingSpace() {
        return type == FilenameErrorType.LEADING_OR_TRAILING_SPACE;
    }

    private enum FilenameErrorType {
        /**
         * 包含非法字符
         */
        CONTAINS_ILLEGAL_CHARACTERS,
        /**
         * 长度不合法
         */
        INVALID_LENGTH,
        /**
         * 以空格开头或结尾
         */
        LEADING_OR_TRAILING_SPACE
    }
}
