package Assem;

import java.util.ArrayList;


public class LiteralTable {

    ArrayList<String> literalList;
    ArrayList<Integer> locationList;

    public LiteralTable() {
        literalList  = new ArrayList<>();
        locationList = new ArrayList<>();
    }
    
    public void putLiteral(String literal) {
    	if (!literalList.contains(literal)) {
            literalList.add(literal);
            locationList.add(null);
        }
    }

    // 필요 메서드 추가 구현
    
    /**
     * 리터럴이 저장된 주소를 반환한다.
     * @param literal 리터럴 문자열
     * @return 저장된 주소, 없거나 아직 설정되지 않았으면 -1
     */
    public int searchLiteral(String literal) {
        int idx = literalList.indexOf(literal);
        if (idx < 0) return -1;
        Integer addr = locationList.get(idx);
        return (addr != null ? addr : -1);
    }

    /**
     * 테이블에 등록된 리터럴 개수를 반환한다.
     */
    public int size() {
        return literalList.size();
    }
    /**
     * 이미 추가된 리터럴에 대해 주소를 설정한다.
     * @param literal  주소를 설정할 리터럴 문자열 (예: "=C'EOF'")
     * @param address  할당할 주소값
     * @throws IllegalArgumentException 해당 리터럴이 테이블에 없으면 예외 발생
     */
    public void setLiteralAddress(String literal, int address) {
        int idx = literalList.indexOf(literal);
        if (idx < 0) {
            throw new IllegalArgumentException("Literal not found: " + literal);
        }
        locationList.set(idx, address);
    }
    
    /**
     * 모든 리터럴 문자열 목록을 반환한다. (원본 보호를 위해 복사 반환)
     */
    public ArrayList<String> getLiterals() {
        return new ArrayList<>(literalList);
    }

    /**
     * 모든 리터럴 주소 목록을 반환한다. (원본 보호를 위해 복사 반환)
     */
    public ArrayList<Integer> getAddresses() {
        return new ArrayList<>(locationList);
    }
}
