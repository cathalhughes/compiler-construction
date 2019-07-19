import java.util.*;

public class SymbolTable extends Object {
	private Hashtable<String, LinkedList<String>> symbolTable;
    private Hashtable<String, String> vals;
    private Hashtable<String, String> types;
    
    SymbolTable() {
        symbolTable = new Hashtable<>();
        vals = new Hashtable<>();
        types = new Hashtable<>();
        symbolTable.put("global", new LinkedList<>());
    }

    public void insert(String id, String value, String type, String scope) {
        LinkedList<String> tmp = symbolTable.get(scope);
        if (tmp == null) { // add new scope
            tmp = new LinkedList<>();
            tmp.add(id);
            symbolTable.put(scope, tmp);
        } else {
            tmp.addFirst(id);
        }
        vals.put(id + scope, value);
        types.put(id + scope, type);
    }

    public void printSymbolTable() {
        String scope;
        Enumeration e = symbolTable.keys();
        while (e.hasMoreElements()) {
            scope = (String) e.nextElement();
            System.out.println("\nScope: " + scope + "\n-------\n");
            LinkedList<String> list = symbolTable.get(scope);
            for (String id : list) {
                String value = vals.get(id + scope);
                String type = types.get(id + scope);
                System.out.print(id + ": " + value + "(" + type + ")" + "\n");
            }
        }
    }

    public Hashtable<String,  ArrayList<String>> getDupsInScopes(){
        Set<String>keys = symbolTable.keySet();
        Hashtable<String, ArrayList<String>> dupsTable = new Hashtable<String, ArrayList<String>>();
        
        for(String key : keys) {
            LinkedList<String> tmpList = symbolTable.get(key);
            ArrayList<String> dups = new ArrayList<String>();
            while(0 < tmpList.size() -1){
                Collections.sort(tmpList);
                if (tmpList.size() > 0) {
                    String checker = tmpList.pop();
                    if(tmpList.contains(checker)){
                        dups.add(checker);
                    }
                }
            }
            dupsTable.put(key, dups);
        }
        
        return dupsTable;
    }
    public ArrayList<String> getType(String id, String scope) {
        LinkedList<String> scopeList = symbolTable.get(scope);
        LinkedList<String> globalList = symbolTable.get("global");
        ArrayList<String> tys = new ArrayList<String>();
        if(scopeList != null) {
            for (String matchingId : scopeList) {
                if(matchingId.equals(id)) {
                    tys.add(types.get(id + scope));    
                }
            }
        }
        
        if(globalList != null && tys.size() == 0) {
            for (String matchingId : globalList) {
                if(matchingId.equals(id)) {
                    tys.add(types.get(id + "global"));    
                }  
            }
        }
        if(tys.size() == 0) {
            tys.add(""); //constant not declared case
        }

        return tys;
    }

    public ArrayList<String> getFunctions() {
        LinkedList<String> list = symbolTable.get("global");
        ArrayList<String> functionNames = new ArrayList<String>();
        for (String func : list){
            if(vals.get(func + "global") != null){
                String functionName = vals.get(func + "global");
                if(functionName.equals("function")){
                    functionNames.add(func);
                }
            }
        }
        return functionNames;
    }
    
    public ArrayList<String> getFunctionParams(String scope) {
        ArrayList<String> paramTypes = new ArrayList<String>();
        LinkedList<String> list = symbolTable.get(scope);
        if(list != null) {
            for (String id : list) {           
                String value = vals.get(id + scope);
                if(value.equals("func parameter")) {
                    paramTypes.add(types.get(id + scope));
                }
            }
        }
        
        return paramTypes;   
    }

    public boolean isConstant(String id , String scope){
        boolean isConstant = false;
        LinkedList<String> globalList = symbolTable.get("global");
        LinkedList<String> list = symbolTable.get(scope);
        if(list != null && list.contains(id)){
            if(vals.get(id + scope).equals("constant")){
                isConstant = true;
            }
        } 
        else if(globalList != null && globalList.contains(id)){
            if(vals.get(id + "global").equals("constant")){
                isConstant = true;
            }

        }
        return isConstant;


    }

    public Hashtable <String, Hashtable< String, ArrayList<Boolean>>> getAllVarsAndConstants() {
        Hashtable <String, Hashtable< String, ArrayList<Boolean>>> scopeVarsAndConsts = new Hashtable <String, Hashtable< String, ArrayList<Boolean>>>();
        String scope;
        Enumeration e = symbolTable.keys();
        while (e.hasMoreElements()) {
            scope = (String) e.nextElement();
            LinkedList<String> list = symbolTable.get(scope);
            Hashtable< String, ArrayList<Boolean>> varsandConsts = new Hashtable< String, ArrayList<Boolean>>();
            for (String id : list) {
                String value = vals.get(id + scope);
                if(value.equals("variable")) {
                    ArrayList<Boolean> varsList = new ArrayList<>(Arrays.asList(false, false));
                    varsandConsts.put(id, varsList);
                }
                else if(value.equals("constant")) {
                    ArrayList<Boolean> constsList = new ArrayList<>(Arrays.asList(true, false));
                    varsandConsts.put(id, constsList);

                }
                
            }
            scopeVarsAndConsts.put(scope, varsandConsts);
        }
        return scopeVarsAndConsts;
    }

    
}