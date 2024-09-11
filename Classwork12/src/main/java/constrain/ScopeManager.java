package constrain;

import ast.AST;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import lexer.Symbol;

class Scope{
  private Map<Symbol, AST> scope = new HashMap<>();
}

public class ScopeManager {

  private Stack<Scope> functionScope = new Stack<>();


  public AST get(){
    return null;
  }

  public void put(Symbol symbol, AST ast){

  }

  public void beginScope(){

  }

  public void endScope(){

  }

}
