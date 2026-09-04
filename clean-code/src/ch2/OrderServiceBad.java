package ch2;

class OrderServiceBad {
    // id, state, cd, amt 만으로는 각각이 어떤 비즈니스 의미를 갖는지 호출부에서 알기 어렵다.
    public void process(long id, String state, String cd, double amt) {

        // 하위 로직을 끝까지 읽어야 'NY 주의 특정 우편번호에 대한 세금 계산'임을 유추할 수 있다.
        if ("NY".equals(state) && cd != null && cd.startsWith("10")) {
            double tax = amt * 0.08;
            double total = amt + tax;
            System.out.println("[Bad] 배송지: " + state + ", 우편번호: " + cd + " -> 총 결제 금액: " + total);
        }
    }
}