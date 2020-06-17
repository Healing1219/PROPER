package frontend;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import backend.ClibCall;


public class TypeSystem {
	private static TypeSystem typeSystem = null;
	public static TypeSystem getTypeSystem() {
		if (typeSystem == null) {
			typeSystem = new TypeSystem();
		}
		return typeSystem;
	}
	
	private TypeSystem() {
		
	}
	
	public HashMap<String, ArrayList<Symbol>> symbolTable = new HashMap<String, ArrayList<Symbol>>();
	
	public ArrayList<Symbol> getSymbolsByScope(String scope) {
		ArrayList<Symbol> list = new ArrayList<Symbol>();
		for (Map.Entry<String, ArrayList<Symbol>> entry : symbolTable.entrySet()) {
			ArrayList<Symbol> args = entry.getValue();
			for (int i = 0; i < args.size(); i++) {
				Symbol sym = args.get(i);
				if (sym.getScope().equals(scope)) {
					list.add(sym);
				}
			}
		}
		
		return list;
	}
	
	public void addSymbolsToTable(Symbol headSymbol, String scope) {
		while (headSymbol != null) {
			headSymbol.addScope(scope);
			
			ArrayList<Symbol> symList = symbolTable.get(headSymbol.name);
			if (symList == null) {
				symList = new ArrayList<Symbol>();
				symList.add(headSymbol);
				symbolTable.put(headSymbol.name, symList);
			}
			else {
				handleDublicateSymbol(headSymbol, symList);
			}
			
			headSymbol = headSymbol.getNextSymbol();
		}
	}
	
	public void removeSymbolFromTable(Symbol symbol) {
		ArrayList<Symbol> symList = symbolTable.get(symbol.name);
		int pos = 0;
		while (pos < symList.size()) {
			Symbol sym = symList.get(pos);
			if (sym.getLevel() == symbol.getLevel()) {
				symList.remove(pos);
				return;
			}
			
			pos++;
		}
	}
	
	public ArrayList<Symbol> getSymbol(String text) {
		return symbolTable.get(text);
	}
	
	private void handleDublicateSymbol(Symbol symbol, ArrayList<Symbol>symList) {
		boolean harmless = true;
		Iterator<Symbol> it = symList.iterator();
		while (it.hasNext()) {
			Symbol sym = it.next();
			if (sym.level == symbol.level) {
				//TODO, handle duplication here
				System.err.println("Symbol definition replicate: " + sym.name);
				System.exit(1);
			}
		}
		
		if (harmless == true) {
			symList.add(symbol);
		}
	}
	
	
	
    public TypeLink newType(String typeText) {
    	Specifier sp = null;
    	int type = Specifier.INT;
    	switch (typeText.charAt(0)) {
    	case 'i':
    		type = Specifier.INT;
    		break;
    	case 'r':
    		type = Specifier.REAL;
    		break;
    	case 'b':
    		type = Specifier.BOOL;
    		break;
    	case 'v':
    		if (typeText.charAt(2) == 'i') {
    			type = Specifier.VOID;
    		}
    		break;
    	}
    	
    	sp = new Specifier();
    	sp.setType(type);
    	
    	TypeLink link = new TypeLink(false, sp);
    	return link;
    }
    
    public void specifierCpy(Specifier dst, Specifier org) {
    	dst.setConstantVal(org.getConstantVal());
    	dst.setOutputClass(org.getOutputClass());
    	dst.setStorageClass(org.getStorageClass());
    }
    
    public TypeLink newClass(String classText) {
    	Specifier sp = new Specifier();
    	sp.setType(Specifier.NONE);
    	setClassType(sp, classText.charAt(0));
    	
    	TypeLink link = new TypeLink(false, sp);
    	return link;
    }
    
    private void setClassType(Specifier sp, char c) {
    	switch(c) {
    	case 0:
    		sp.setStorageClass(Specifier.FIXED);
    		break;
    	/*case 't':
    		sp.setStorageClass(Specifier.TYPEDEF);
    		break;
    	case 'r':
    		sp.setStorageClass(Specifier.REGISTER);
    		break;
    	case 's':
    		sp.setStatic(true);
    		break;
    	case 'e':
    		sp.setExternal(true);
    		break;*/
    		
    	default:
    			System.err.println("Internal error, Invalid Class type");
    			System.exit(1);
    			break;
     	}
    }
    
    public Symbol newSymbol(String name, int level) {
    	return new Symbol(name, level);
    }
    
    public void addDeclarator(Symbol symbol, int declaratorType) {
    	Declarator declarator = new Declarator(declaratorType);
    	TypeLink link = new TypeLink(true, declarator);
    	symbol.addDeclarator(link);
    }
    
    public void addSpecifierToDeclaration(TypeLink specifier, Symbol symbol) {
    	while (symbol != null) {
    		symbol.addSpecifier(specifier);
    		symbol = symbol.getNextSymbol();
    	}
    }
    
    public Symbol getSymbolByText(String text, int level) {
    	
    	ClibCall libCall = ClibCall.getInstance();
    	if (libCall.isAPICall(text)) {
    		Symbol symbol=new Symbol(text, level);
    		//addDeclarator(symbol, Declarator.FUNCTION);
    		//symbol.setValue(ClibCall.invokeAPI(text));
    	    return symbol;	
    	}
    	
    	ArrayList<Symbol> symbolList = typeSystem.getSymbol(text);
    	int i = 0;
    	Symbol symbol = null;
    	
    	while (symbolList!=null&&i < symbolList.size()) {
    		symbol = symbolList.get(0);
    		if (symbolList.get(i).getLevel() == level) {
    			return symbolList.get(i);
    		} else if (symbolList.get(i).getLevel() >= symbol.getLevel()) {
    			symbol = symbolList.get(i);
    		}
    		i++;
    	}
    	return symbol;
    }
}
