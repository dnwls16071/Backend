package ch2;

class OrderServiceGood {
    // 변수명에 명확한 맥락(order, delivery, zip, product)을 붙여 오해의 소지를 제거했다.
    public void processOrder(long orderId, String deliveryState, String zipCode, double productAmount) {

        // 인자만 보아도 어떤 데이터가 매개변수로 들어오는지 직관적으로 파악된다.
        if ("NY".equals(deliveryState) && zipCode != null && zipCode.startsWith("10")) {
            double regionalTax = productAmount * 0.08;
            double totalOrderAmount = productAmount + regionalTax;
            System.out.println("[Better] 배송지: " + deliveryState + ", 우편번호: " + zipCode + " -> 총 결제 금액: " + totalOrderAmount);
        }
    }
}