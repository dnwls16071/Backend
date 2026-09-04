package ch2;

record Address(
    String state, // Address 컨텍스트 내부에 존재하므로 당연히 '배송지 주(State)'를 의미하게 됨
    ZipCode zipCode
) {
}
