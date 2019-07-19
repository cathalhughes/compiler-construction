import java.util.*;

public class PrintVisitor implements CALParserVisitor {

  private static boolean arithmeticOperatorCheck = true;
  private static boolean functionForEveryFunctionCall = true;
  private static boolean correctNumberOfArgs = true;
  private static boolean argsOfCorrectType = true;
  private static boolean writtenAndReadCheck = true;
  private static boolean noDups = true;
  private static boolean nonVoidReturnCheck = true;
  private static boolean voidReturnCheck = true;
  private static boolean constantCheck = true; 
  private static boolean returnTypeCheck = true;
  private static boolean conditionCheck = true;
  private static boolean comparisonCheck = true;
  private static boolean functionDoesNotExist = true;
  private static boolean varsCheck = true;
  private static boolean declaredInScope = true;
  private static boolean logicalCheck = true;

  private static int numPassed = 0;

  private static String scope;
  private static SymbolTable st;
  private static ArrayList<String> functionNames;
  private static List<String> errorList = new ArrayList<String>();
  private static ArrayList<String> dupList = new ArrayList<String>();
  ArrayList<String> parents = new ArrayList<String>(Arrays.asList("Arg_List" , "FunctionReturn", "Minus_Operator", "Plus_Operator", "Negative", "EqOP","NotEqOP","LessThanOP","LessOrEqualOP","GreaterThanOP","GreaterOrEqualOP"));
  


  private Hashtable <String, Hashtable< String, ArrayList<Boolean>>> writtenToAndReadFromTable;
  

  @Override
  public Object visit(SimpleNode node, Object data){
  	throw new RuntimeException("Visit SimpleNode");
  }
  @Override
  public Object visit(ASTProgram node, Object data){
    scope = "global";
  	st = (SymbolTable) data;
    functionNames = st.getFunctions();
    writtenToAndReadFromTable = st.getAllVarsAndConstants();
      
    node.childrenAccept(this, data);
    if(functionNames.size() > 0) {
     

      functionForEveryFunctionCall = false;
      for(String functionName : functionNames) {
        //System.out.println("\t" + "Function " + functionName + " has not been called");
        errorList.add( "Function " + functionName + " has not been called");
      }
    }
    Hashtable<String, ArrayList<String>> dups = st.getDupsInScopes();
    if(dups.size() > 0){
      Enumeration e = dups.keys();
      while(e.hasMoreElements()) {
        String scope = (String) e.nextElement();
        dupList = dups.get(scope);
        Set<String> dupsSet = new LinkedHashSet<String>(dupList);
        Iterator<String> it = dupsSet.iterator();
        while(it.hasNext()) {
          errorList.add("Duplicates in scope " + scope + ". Identifier: " + it.next());
          noDups = false;
        }
      }
      
    } 

    writtenAndRead();
    System.out.println("--------------------------------------");
    Set<String> errors=new LinkedHashSet<String>(errorList);
    Iterator<String> itr = errors.iterator();
    
    
    System.out.println("Semantic Checks Passed: \n");

    if (declaredInScope) {
      System.out.println("ID declared before used Check: Pass");
      numPassed++;
    }
    if (constantCheck) {
      System.out.println("Constant Type Check: Pass");
      numPassed++;
    }
    if (varsCheck) {
      System.out.println("Variable Type Check: Pass");
      numPassed++;
    }
    if (arithmeticOperatorCheck) {
      System.out.println("Arithmetic Args Check: Pass");
      numPassed++;
    }    
    if (comparisonCheck) {
      System.out.println("Boolean Check: Pass");
      numPassed++;
    }
    if (noDups) {
      System.out.println("Duplicates in scope Check: Pass");
      numPassed++;
    }
    if (functionForEveryFunctionCall){
      System.out.println("All Functions have been called Check: Pass");
      numPassed++;
    }
    if (correctNumberOfArgs) {
      System.out.println("All Functions have correct number of arguments check: Pass");
      numPassed++;
    } 
    if (argsOfCorrectType) {
      System.out.println("All functions have arguments of correct type Check: Pass");
      numPassed++;
    }
    if (writtenAndReadCheck) {
      System.out.println("All variables have been written to and read from Check: Pass");
      numPassed++;
    } 
    if (nonVoidReturnCheck) {
      System.out.println("All non-void functions make a return check: Pass");
      numPassed++;
    }
    if (voidReturnCheck) {
      System.out.println("All void functions do not make a return Check: Pass");
      numPassed++;
    }
    if (returnTypeCheck) {
      System.out.println("All functions have the correct return type Check: Pass");
      numPassed++;
    }
    if (conditionCheck) {
      System.out.println("Conditions are valid Check: Pass");
      numPassed++;
    }
    if (functionDoesNotExist) {
      System.out.println("All invoked functions exist check Check: Pass");
      numPassed++;
    }
    if (logicalCheck) {
      System.out.println("All Logical Operators have valid conditions: Pass");
      numPassed++;
    }

    System.out.println("--------------------------------------");
    System.out.println(numPassed + "/16 Semantic Checks Passed!");
    System.out.println("--------------------------------------");

    System.out.println("Errors: ");
    System.out.println("--------------------------------------");
    while(itr.hasNext()) {
      System.out.println(itr.next());
    }

    System.out.println("--------------------------------------");
    System.out.println("There are " + errors.size() + " errors!");

    return DataType.prog;
  }

  @Override
  public Object visit(ASTDeclaration_List node, Object data){
    node.childrenAccept(this, data);
  	return data;

  }

  @Override
  public Object visit(ASTVariable_Declaration node, Object data) {
    node.childrenAccept(this, data);
    return DataType.var_decl;
  }

  @Override
  public Object visit(ASTConstant_Declaration node, Object data){
    SimpleNode child1 = (SimpleNode) node.jjtGetChild(0);
    DataType child2Type = (DataType) node.jjtGetChild(1).jjtAccept(this, data);
    DataType child3Type = (DataType) node.jjtGetChild(2).jjtAccept(this, data);
    if(child2Type != child3Type){
      constantCheck = false;
      errorList.add("Error in constant declaration in " + scope + ", "  + child1.value + ", assigning types do not correspond. LHS is " + child2Type + " while RHS is " + child3Type);
    }
  	return DataType.const_decl;

  }
  @Override
  public Object visit(ASTIdentifier node, Object data){
    SimpleNode parent = (SimpleNode) node.jjtGetParent();
    String nodeValue = (String) node.value;
    if(parents.contains(parent.toString())){
      addToWrittenAndRead(scope, nodeValue, 1);
      
    }   
    String value = (String) node.jjtGetValue();
    
    if(parent.toString() == "Variable_Declaration"){
        return DataType.var_decl;
    }
    else if(parent.toString() == "Constant_Declaration"){
        return DataType.const_decl;
    }
    else if(parent.toString() == "Function"){
        String functionId = (String)node.jjtGetValue();
        scope = functionId;
        return DataType.function;
    }
    else if(st.getType(value, scope).contains("")) {
      errorList.add("Identifier " + nodeValue + " in scope " + scope + " is not declared before use");
      return DataType.type_unknown;
    }
    else if(st.getType(value, scope).contains("integer")) {
      return DataType.Num;
    }
    else if(st.getType(value, scope).contains("boolean")) {
      return DataType.bool;
    }

    return DataType.type_unknown;

  }
  @Override
  public Object visit(ASTNumber node, Object data){
  	return DataType.Num;

  }
  @Override
  public Object visit(ASTFunction_List node, Object data){
  	node.childrenAccept(this, data);
  	return data;

  }

  @Override
  public Object visit(ASTFunction node, Object data){
      SimpleNode function = (SimpleNode) node.jjtGetChild(1);
      String functionName = (String) function.value;
      DataType functionType = (DataType) node.jjtGetChild(0).jjtAccept(this, data);
      ASTFunctionReturn returnType = (ASTFunctionReturn) node.jjtGetChild(5);

      node.childrenAccept(this, data);
      returnTypeChecks(returnType, functionType, functionName, data);
      scope = "global";
      return DataType.function;
  }

  @Override
  public Object visit(ASTFunctionReturn node, Object data){

    node.childrenAccept(this, data);
    return DataType.function;

  }


  @Override
  public Object visit(ASTType node, Object data){
  	String s = (String)node.jjtGetValue();
    if(s.equalsIgnoreCase("boolean")){
        return DataType.bool;
    }
    if(s.equalsIgnoreCase("void")){
        return DataType.type_unknown;
    }
    if(s.equalsIgnoreCase("integer")){
        return DataType.Num;
    }
    return DataType.type_unknown;

  }
  @Override
  public Object visit(ASTParameter_List node, Object data){
  	node.childrenAccept(this, data);
  	return data;

  }
  @Override
  public Object visit(ASTParameter node, Object data){
    node.childrenAccept(this, data);
  	return data;

  }
  @Override
  public Object visit(ASTMain node, Object data){
    scope = "main";
  	node.childrenAccept(this, data);
  	return data;

  }
  @Override
  public Object visit(ASTStatement_Block node, Object data){
  	node.childrenAccept(this, data);
  	return data;

  }
  @Override
  public Object visit(ASTAssignment node, Object data){

    SimpleNode child1 = (SimpleNode) node.jjtGetChild(0);
    String child1Value = (String) child1.value;

    addToWrittenAndRead(scope, child1Value, 0);
    
    

    if (st.isConstant(child1Value,scope)){
      constantCheck = false;
      //System.out.println("Constant " + child1Value + " can not be assigned a new value");
      errorList.add("Constant " + child1Value + " in scope " + scope + " can not be assigned a new value");
      
    }
    SimpleNode child2 = (SimpleNode) node.jjtGetChild(1);
    String child2Value = (String) child2.value;
    if(child2Value != null) {
      addToWrittenAndRead(scope, child2Value, 1);
      
    }

    DataType child1DataType = (DataType) child1.jjtAccept(this, data);
    DataType child2DataType = (DataType) child2.jjtAccept(this, data);
    if (child1DataType == child2DataType && child1DataType != DataType.type_unknown) {
      return DataType.assign;
    }
    else if (child1DataType == DataType.type_unknown) {
        declaredInScope = false;
        errorList.add("Identifier " + child1Value + " in scope " + scope + " is not declared before use");
        
    }
    else {
      varsCheck = false;
      errorList.add("Error in " + scope +", assigning types do not correspond. Cannot assign " + child2DataType + " to " + child1DataType);
      
    }
  	return DataType.type_unknown;

  }

  @Override
  public Object visit(ASTStatement node, Object data){
  	node.childrenAccept(this, data);
  	return data;

  }
  @Override
  public Object visit(ASTSkip node, Object data){
  	return data;

  }
  @Override
  public Object visit(ASTMinus_Operator node, Object data){
  	SimpleNode firstChild = (SimpleNode) node.jjtGetChild(0);
    SimpleNode secondChild = (SimpleNode) node.jjtGetChild(1);
    DataType firstChildDataType = (DataType) firstChild.jjtAccept(this, data);
    DataType secondChildDataType = (DataType) secondChild.jjtAccept(this, data);
        if(firstChildDataType != DataType.Num | secondChildDataType != DataType.Num) {
            arithmeticOperatorCheck = false;
            errorList.add("Error in " + scope + ", Arithmetic Operator Check Failed: Cannot subtract '" + firstChildDataType +
                    "' and '" + secondChildDataType + "'");
            return DataType.type_unknown;
          }

        return DataType.Num;
  }

  @Override
  public Object visit(ASTPlus_Operator node, Object data) {
      SimpleNode firstChild = (SimpleNode) node.jjtGetChild(0);
      SimpleNode secondChild = (SimpleNode) node.jjtGetChild(1);
      DataType firstChildDataType = (DataType) firstChild.jjtAccept(this, data);
      DataType secondChildDataType = (DataType) secondChild.jjtAccept(this, data);
      if ((firstChildDataType != secondChildDataType) | firstChildDataType != DataType.Num  | secondChildDataType != DataType.Num ) {
          arithmeticOperatorCheck = false;
          errorList.add("Error in " + scope + ", Arithmetic Operator Check Failed: Cannot add '" + firstChildDataType +
                  "' and '" + secondChildDataType + "'");
          return DataType.type_unknown;
      }

      return DataType.Num;
  }

  @Override
  public Object visit(ASTFunctionCall node, Object data){
        SimpleNode firstChild = (SimpleNode) node.jjtGetChild(0);
        if(st.getFunctions().contains(((String)firstChild.value))){
          ArrayList<String> argsType = new ArrayList<String>();
          argsType = checkArgumentsTypes((ASTArg_List) node.jjtGetChild(1),data, argsType);
          Collections.reverse(argsType);
          ArrayList<String> functionParamTypes = st.getFunctionParams((String)firstChild.value);
          functionSemanticChecks(argsType, functionParamTypes, firstChild);
          DataType firstChildDataType = (DataType) firstChild.jjtAccept(this, data);
          node.childrenAccept(this, data);
          if(functionNames.contains((String)firstChild.value)) {
            functionNames.remove((String)firstChild.value);
          }
          if(st.getType((String)firstChild.value, "global").contains("integer")) {
            return DataType.Num;
          }
          else if(st.getType((String)firstChild.value, "global").contains("boolean")) {
            return DataType.bool;
          }
          else if(st.getType((String)firstChild.value, "global").contains("void")) {
            return DataType.type_unknown;
          }
        } else {
          functionDoesNotExist = false;
          errorList.add("Error, function: " + (String)firstChild.value + " does not exist");
          
        }

        return DataType.type_unknown; 

 }


  @Override
  public Object visit(ASTAnd_Operator node, Object data){
    DataType child1DataType = (DataType) node.jjtGetChild(0).jjtAccept(this, data);
    DataType child2DataType = (DataType) node.jjtGetChild(1).jjtAccept(this, data);
    if(child1DataType != DataType.comp_op || child2DataType != DataType.comp_op) {
      errorList.add("Error in Logical Operator in scope " + scope + ". Cannot " + node.value + " " + child1DataType + " and " + child2DataType);
      logicalCheck = false;
    }
  	
  	return data;

  }
  @Override
  public Object visit(ASTOr_Operator node, Object data){
  	DataType child1DataType = (DataType) node.jjtGetChild(0).jjtAccept(this, data);
    DataType child2DataType = (DataType) node.jjtGetChild(1).jjtAccept(this, data);
    if(child1DataType != DataType.comp_op || child2DataType != DataType.comp_op) {
      errorList.add("Error in Logical Operator in scope " + scope + ". Cannot " + node.value + " " + child1DataType + " and " + child2DataType);
      logicalCheck = false;
    }
  	return data;

  }
  @Override
  public Object visit(ASTNegative node, Object data){
  	node.childrenAccept(this, data);
  	return DataType.Num;

  }
  @Override
  public Object visit(ASTBoolean node, Object data){
  	node.childrenAccept(this, data);
  	return DataType.bool;

  }
  @Override
  public Object visit(ASTEqOP node, Object data){
  	DataType child1DataType = (DataType) node.jjtGetChild(0).jjtAccept(this, data);
    DataType child2DataType = (DataType) node.jjtGetChild(1).jjtAccept(this, data);
    if(operationCheck(child1DataType, child2DataType, node)) {
      return DataType.comp_op;
    }
  	return DataType.type_unknown;

  }
  @Override
  public Object visit(ASTNotEqOP node, Object data){
  	DataType child1DataType = (DataType) node.jjtGetChild(0).jjtAccept(this, data);
    DataType child2DataType = (DataType) node.jjtGetChild(1).jjtAccept(this, data);
    if(operationCheck(child1DataType, child2DataType, node)) {
      return DataType.comp_op;
    }
    return DataType.type_unknown;

  }
  @Override
  public Object visit(ASTLessThanOP node, Object data){
  	DataType child1DataType = (DataType) node.jjtGetChild(0).jjtAccept(this, data);
    DataType child2DataType = (DataType) node.jjtGetChild(1).jjtAccept(this, data);
    if(integerOperationCheck(child1DataType, child2DataType, node)) {
      return DataType.comp_op;
    }
    return DataType.type_unknown;

  }
  @Override
  public Object visit(ASTLessOrEqualOP node, Object data){
  	DataType child1DataType = (DataType) node.jjtGetChild(0).jjtAccept(this, data);
    DataType child2DataType = (DataType) node.jjtGetChild(1).jjtAccept(this, data);
    if(integerOperationCheck(child1DataType, child2DataType, node)) {
      return DataType.comp_op;
    }
    return DataType.type_unknown;

  }
  @Override
  public Object visit(ASTGreaterThanOP node, Object data){
  	DataType child1DataType = (DataType) node.jjtGetChild(0).jjtAccept(this, data);
    DataType child2DataType = (DataType) node.jjtGetChild(1).jjtAccept(this, data);
    if(integerOperationCheck(child1DataType, child2DataType, node)) {
      return DataType.comp_op;
    }
    return DataType.type_unknown;

  }
  @Override
  public Object visit(ASTGreaterOrEqualOP node, Object data){
    DataType child1DataType = (DataType) node.jjtGetChild(0).jjtAccept(this, data);
    DataType child2DataType = (DataType) node.jjtGetChild(1).jjtAccept(this, data);
    if(integerOperationCheck(child1DataType, child2DataType, node)) {
      return DataType.comp_op;
    }
    return DataType.type_unknown;

  }
  @Override
  public Object visit(ASTArg_List node, Object data){
  	node.childrenAccept(this, data);
  	return data;

  }

  private boolean integerOperationCheck(DataType child1, DataType child2, SimpleNode node) {
    
    boolean check = true;
    if(child1 != DataType.Num | child2 != DataType.Num){
      check = false;
      comparisonCheck = false;
      errorList.add("Error in " + scope + ", Comparsion Check Failed!" + " Cannot compare '" + child1 + "' to '" + child2 + "' using " + node.value);
      
    }
    return check;
  }

  private boolean operationCheck(DataType child1, DataType child2, SimpleNode node) {
    
    boolean check = true;
    if((child1 != DataType.bool | child2 != DataType.bool) & (child1 != DataType.Num | child2 != DataType.Num)){
      check = false;
      conditionCheck = false;
      errorList.add("Error in " + scope + ", Boolean Check Failed!" + " Cannot compare '" + child1 + "' to: '" + child2 + "' using " + node.value);
    }
    return check;
  }

  private ArrayList<String> checkArgumentsTypes(ASTArg_List node, Object data, ArrayList<String>argsType){
      if(node.jjtGetNumChildren() != 0) {
      SimpleNode val = (SimpleNode) node.jjtGetChild(0);
      //System.out.println((String)val.value);
      if((DataType) node.jjtGetChild(0).jjtAccept(this, data) == DataType.Num) {
        argsType.add("integer");  
      }
      else if((DataType) node.jjtGetChild(0).jjtAccept(this, data) == DataType.bool) {
        argsType.add("boolean");  
      }
      else {
        argsType.add("unknown");
      }
      
      checkArgumentsTypes((ASTArg_List)node.jjtGetChild(1), data, argsType);
    }

    return argsType;
    
  }

  public void writtenAndRead() {
        Enumeration e = writtenToAndReadFromTable.keys();
        String scopeKey;
        while(e.hasMoreElements()) {
          scopeKey = (String) e.nextElement();
          Enumeration e1 = writtenToAndReadFromTable.get(scopeKey).keys();
          while(e1.hasMoreElements()) {
            String id = (String) e1.nextElement();
            ArrayList<Boolean> writtenAndRead = writtenToAndReadFromTable.get(scopeKey).get(id);
            if(writtenAndRead.get(0) == false){
              writtenAndReadCheck = false;
              errorList.add(id + ", in scope " + scopeKey + ", has not been written to");
              
            }
            if(writtenAndRead.get(1) == false) {
              writtenAndReadCheck = false;
              errorList.add(id + ", in scope " + scopeKey + ", has not been read from");
              
            }

          }
        }
    }

    public void addToWrittenAndRead(String scope, String value, int index) {
        ArrayList<Boolean> writtenAndRead = new ArrayList<Boolean>();
        if(writtenToAndReadFromTable.get(scope) != null && writtenToAndReadFromTable.get(scope).get(value) != null){
          writtenAndRead = writtenToAndReadFromTable.get(scope).get(value); 
          Boolean boolValue = true;
          writtenAndRead.set(index,boolValue);
          writtenToAndReadFromTable.get(scope).put(value, writtenAndRead);
        }
        else if(writtenToAndReadFromTable.get("global").get(value) != null){
            writtenAndRead = writtenToAndReadFromTable.get("global").get(value); 
            Boolean boolValue = true;
            writtenAndRead.set(index,boolValue);
            writtenToAndReadFromTable.get("global").put(value, writtenAndRead);
          }
    }

    public void functionSemanticChecks(ArrayList<String> argsType, ArrayList<String> functionParamTypes, SimpleNode firstChild) {
    if(argsType.contains("unknown")) {
        declaredInScope = false;
        errorList.add("Identifier passed in as argument to function is not declared before use");
        
      }
      else if (argsType.size() != functionParamTypes.size()) {
        correctNumberOfArgs = false;
        errorList.add("Error, function " + (String)firstChild.value +  " called with incorrect number of arguments. Got " + argsType.size() + " arguments, expected " + functionParamTypes.size());
        
      }
      else if(!argsType.equals(functionParamTypes)) {
        argsOfCorrectType = false;
        errorList.add("Error, function  " + (String)firstChild.value +  "  called with arguments of incorrect type.\n\tGot types: " + argsType + "\n\tExpected:  " +  functionParamTypes);
        
      }
  }

  private void returnTypeChecks(ASTFunctionReturn returnType, DataType functionType, String functionName, Object data) {
    if(returnType.jjtGetNumChildren() == 0) {
      if(functionType != DataType.type_unknown){
        nonVoidReturnCheck = false;
        errorList.add("Error non void " + functionName + " function must return something");
        
      }
    }
    else{
      if(functionType == DataType.type_unknown) {
        voidReturnCheck = false;
        errorList.add("Void function " + functionName + " should not be returning a value");
        
      }
      else {
        DataType returnTypeData = (DataType) returnType.jjtGetChild(0).jjtAccept(this, data);
        if(functionType  != returnTypeData) {
          returnTypeCheck = false;
          errorList.add("Error in function " + functionName + " , function type does not match return type. Function type is " + functionType + ", Return type is " + returnTypeData);
          
        }
      }
    }

  }
  
	
}