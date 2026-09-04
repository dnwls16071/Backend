package ch3.payroll;

public class HourlyEmployee extends Employee {
    private final Money hourlyRate;
    private final double hoursWorked;

    public HourlyEmployee(EmployeeRecord record) {
        super(record);
        this.hourlyRate = new Money(record.hourlyRate != null ? record.hourlyRate : "0");
        this.hoursWorked = record.hoursWorked;
    }

    @Override
    public boolean isPayday() {
        // 시급제 직원의 주급 정산일 판별 로직 (대충 구현)
        return true;
    }

    @Override
    public Money calculatePay() {
        // 시급 * 근무시간 연산 후 반환
        return hourlyRate.multiply(hoursWorked);
    }

    @Override
    public void deliverPay(Money pay) {
        System.out.println("[시급제] " + name + "님에게 " + pay + "이 계좌이체 되었습니다.");
    }
}