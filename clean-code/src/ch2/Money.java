package ch2;

record Money(double amount) {
    public Money plus(Money other) {
        return new Money(this.amount + other.amount);
    }

    public Money times(double percent) {
        return new Money(this.amount * percent);
    }
}
