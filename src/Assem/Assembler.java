package Assem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;  

/**
 * Assembler : 
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. <br>
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. <br>
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. <br>
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) <br>
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) <br>
 * 
 * <br><br>
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.<br>
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<LiteralTable> literalList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간*/
	ArrayList<TokenTable> TokenList;
	
	ArrayList<Section> sections = new ArrayList<>();
	/** 
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간. <br>
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;
	
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름. 
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literalList = new ArrayList<LiteralTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/** 
	 * 어셐블러의 메인 루틴
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst_table.txt");
		System.out.printf("%02x\n",assembler.instTable.get_opcode("LDA"));
		System.out.println("asdf");
		
		
		assembler.loadInputFile("input.txt");
		//System.out.println(assembler.lineList);
		
		
		assembler.pass1();
		assembler.printSymbolTable("output_symtab.txt");
		assembler.printLiteralTable("output_littab.txt");
		
		
		assembler.pass2();
		assembler.printObjectCode("output_objectcode.txt");
		
	}


	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.<br>
	 * @param inputFile : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		// TODO Auto-generated method stub
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
            	lineList.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading inst file “" + inputFile + "”: " + e.getMessage(), e);
        }

	}

	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.<br>
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
	       for(int k=0;k<3;k++) {
	    	   // symTab은 pass1에서 사용한 심볼 테이블 인스턴스
		        ArrayList<String> symbols   = symtabList.get(k).getSymbols();
		        ArrayList<Integer> locations = symtabList.get(k).getLocations();
		        for (int i = 0; i < symbols.size(); i++) {
		            String name = symbols.get(i);
		            int addr    = locations.get(i);
		            // 4자리 16진수 대문자 포맷
		            bw.write(name + "\t" + String.format("%04X", addr));
		            bw.newLine();
		        }
	       }
	    } catch (IOException e) {
	        System.err.println("Error writing symbol table: " + e.getMessage());
	    }
	}

	/**
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.<br>
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		// TODO Auto-generated method stub
		  try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
		        // lit 은 Assembler 필드로 선언된 LiteralTable 인스턴스
			  for (LiteralTable lt : literalList) {
				  	ArrayList<String> literals   = lt.getLiterals();
			        ArrayList<Integer> addresses = lt.getAddresses();
			        for (int i = 0; i < literals.size(); i++) {
			            String litStr = literals.get(i);
			            Integer addr  = addresses.get(i);
			            String addrField = (addr != null && addr >= 0)
			                               ? String.format("%04X", addr)
			                               : "";
			            bw.write(litStr + "\t" + addrField);
			            bw.newLine();
			        }
			  }
		        
		    } catch (IOException e) {
		        System.err.println("Error writing literal table: " + e.getMessage());
		    }
	}

	/** 
	 * pass1 과정을 수행한다.<br>
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성<br>
	 *   2) label을 symbolTable에 정리<br>
	 *   <br><br>
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		// 1) locctr 초기화
	    int locctr = 0;
	    
	    // 2) literal 처리 플래그 및 시작 인덱스
	    boolean literalFlag = false;
	    int literalStart = 0;
	    
	    // 3) 한 섹션 전용 심볼·리터럴·토큰 테이블
	    SymbolTable symTab     = new SymbolTable();
	    LiteralTable litTab    = new LiteralTable();
	    TokenTable tokenTable  = new TokenTable(symTab, instTable,litTab);
	    symtabList.add(symTab);
	    literalList.add(litTab);  
	    TokenList.add(tokenTable);

	    // 4) 입력 라인 하나씩 처리
	    for (String line : lineList) {
	        
	        System.out.println(line);
	        // 토큰화
	        tokenTable.putToken(line);
	        Token t = tokenTable.getToken(tokenTable.size() - 1);
	        System.out.println(t.label + "\t" + t.operator +"\t"+ Arrays.toString(t.operand));
	        // 4-1) START
	        if (t.operator != null && t.operator.equals("START")) {
	            // operand[0] 에 시작 주소(10진수) 저장
	            locctr = Integer.parseInt(t.operand[0]);
	            t.location = locctr;
	            // 심볼테이블에 프로그램 이름(label) 등록
	            symTab.putSymbol(t.label, locctr);
	            continue;
	        }

	        // 4-2) LTORG / END 에서 미처리된 리터럴 할당
	        if (t.operator != null && (t.operator.equals("END") || t.operator.equals("LTORG"))) {
	            if (literalFlag) {
	                for (int j = literalStart; j < litTab.size(); j++) {
	                    String lit = litTab.getLiterals().get(j);
	                    // 현재 locctr을 리터럴 주소로 설정
	                    litTab.setLiteralAddress(lit, locctr);
	                    // 길이에 따라 locctr 증가 (C/X 형식)
	                    if (lit.charAt(1) == 'C')
	                        locctr += (lit.length() - 4);
	                    else if (lit.charAt(1) == 'X')
	                        locctr += (lit.length() - 4) / 2;
	                }
	                literalFlag = false;
	            }
	        }

	        // 4-3) CSECT: 새로운 섹션 시작
	        if (t.operator != null && t.operator.equals("CSECT")) {
	        	// 새 테이블 생성
	            symTab  = new SymbolTable();
	            litTab  = new LiteralTable();
	            tokenTable  = new TokenTable(symTab, instTable, litTab);
	            // 리스트에 추가
	            symtabList   .add(symTab);
	            literalList   .add(litTab);
	            TokenList .add(tokenTable);
	            
	            // locctr 초기화, 이 토큰에 주소 저장, 심볼테이블에 CSECT 이름 등록
	            locctr = 0;
	            t.location = locctr;
	            symTab.putSymbol(t.label, locctr);
	            continue;
	        }

	        // 4-4) 라벨이 있으면 심볼테이블에 등록
	        if (t.label != null
	         && !t.label.isEmpty()
	         && t.label.charAt(0) != '.'
	         && t.label.charAt(0) != '*') {
	            symTab.putSymbol(t.label, locctr);
	        }

	        // 4-5) 리터럴 발견 시 테이블에 추가 (중복 체크 후)
	        if (t.operand[0] != null && t.operand[0].startsWith("=")) {
	            if (litTab.searchLiteral(t.operand[0]) == -1) {
	                litTab.putLiteral(t.operand[0]);
	                // 다음 LTORG/END 때 처리할 시작 인덱스 설정
	                literalFlag = true;
	                literalStart = litTab.size() - 1;
	            }
	        }

	        // 4-6) 이 토큰의 주소 저장
	        t.location = locctr;

	        // 4-7) format4(+) 체크 및 mnemonic 분리
	        boolean isFormat4 = false;
	        String op = t.operator;
	        if (op != null && op.startsWith("+")) {
	            isFormat4 = true;
	            op = op.substring(1);
	        }

	        // 4-8) 명령어 길이 계산
	        Instruction inst = instTable.getInstruction(op);
	        if (inst != null) {
	            // format 2: 2바이트, 나머지는 3 또는 4바이트
	            if (inst.format == 2) {
	                locctr += 2;
	            } else {
	                locctr += (isFormat4 ? 4 : 3);
	            }
	        }
	        else if ("RESW".equals(op)) {
	            locctr += 3 * Integer.parseInt(t.operand[0]);
	        }
	        else if ("RESB".equals(op)) {
	            locctr += Integer.parseInt(t.operand[0]);
	        }
	        else if ("WORD".equals(op)) {
	            locctr += 3;
	        }
	        else if ("BYTE".equals(op)) {
	            locctr += 1;
	        }
	        // 그 외는 locctr 변화 없음 (EQU, comment 등)
	    }
		
	}
	
	/**
	 * pass2 과정을 수행한다.<br>
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
		 codeList.clear();

		    
		    for (int sec = 0; sec < TokenList.size(); sec++) {
		        TokenTable   tokTab = TokenList   .get(sec);
		        SymbolTable  symTab = symtabList   .get(sec);
		        LiteralTable litTab = literalList  .get(sec);

		        // 이 TokenTable.makeObjectCode 내부에서 
		        // symTab.searchSymbol(), litTab.searchLiteral() 만 쓰게 됩니다.
		        for (int i = 0; i < tokTab.size(); i++) {
		            tokTab.makeObjectCode(i);
		            String obj = tokTab.getObjectCode(i);
		            if (obj != null && !obj.isEmpty()) {
		                codeList.add(obj);
		            }
		        }
		        System.out.println(codeList);
		        
		    }
	}

	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.<br>
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		
	}
}
