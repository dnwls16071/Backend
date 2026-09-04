package ch3.payroll;

public class Payroll {

    public Money calculatePay(Employee e) throws InvalidEmployeeType {
        switch (e.type) {
            case COMMISSIONED:
                return calculateCommissionedPay(e);
            case HOURLY:
                return calculateHourlyPay(e);
            case SALARIED:
                return calculateSalariedPay(e);
            default:
                throw new InvalidEmployeeType(e.type);
        }
    }

    private Money calculateCommissionedPay(Employee e) { return new Money(); }
    private Money calculateHourlyPay(Employee e) { return new Money(); }
    private Money calculateSalariedPay(Employee e) { return new Money(); }
}