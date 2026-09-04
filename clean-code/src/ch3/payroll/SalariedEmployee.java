package ch3.payroll;

public class SalariedEmployee extends Employee {
    private final Money baseSalary;

    public SalariedEmployee(EmployeeRecord record) {
        super(record);
        this.baseSalary = new Money(record.baseSalary != null ? record.baseSalary : "0");
    }

    @Override
    public boolean isPayday() {
        // 매달 25일 정산 판별 로직
        return true;
    }

    @Override
    public Money calculatePay() {
        // 고정 기본급 반환
        return baseSalary;
    }

    @Override
    public void deliverPay(Money pay) {
        System.out.println("[월급제] " + name + "님에게 " + pay + "이 계좌이체 되었습니다.");
    }
}