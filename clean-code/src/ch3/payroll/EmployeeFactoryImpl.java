package ch3.payroll;

public class EmployeeFactoryImpl implements EmployeeFactory {

    @Override
    public Employee makeEmployee(EmployeeRecord r) throws InvalidEmployeeType {
        switch (r.type) {
            case HOURLY:
                return new HourlyEmployee(r);
            case SALARIED:
                return new SalariedEmployee(r);
            case COMMISSIONED:
                return new CommissionedEmployee(r);
            default:
                throw new InvalidEmployeeType(r.type);
        }
    }
}