package ch2;

import java.util.Objects;

record ZipCode(String value) {
    public ZipCode {
        Objects.requireNonNull(value, "우편번호는 null일 수 없습니다.");
        if (value.length() != 5) {
            throw new IllegalArgumentException("우편번호는 반드시 5자리여야 합니다.");
        }
    }

    public boolean isNewYorkArea() {
        return value.startsWith("10");
    }
}