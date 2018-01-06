import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.*;
class Argument{
	String value;
	boolean isVariable;
	Argument(String value, boolean isVariable){
		this.value = value;
		this.isVariable = isVariable;
	}
	Argument(Argument arg){
		value = new String(arg.value);
		isVariable = arg.isVariable;
	}
	public String toString(){
		return value;
	}
}
class Predicate{
	String name;
	Argument[] argList;
	boolean isNegated;
	Predicate(String name, Argument[] argList, boolean isNegated){
		this.name = name;
		this.argList = argList;
		this.isNegated = isNegated;
	}
	Predicate(Predicate pred){
		name = new String(pred.name);
		isNegated = pred.isNegated;
		argList = new Argument[pred.argList.length];
	}
	public String toString(){
		String str = (isNegated)? "~":"";
		str+=name+"(";
		for(int i=0; i<argList.length; ++i){
			String end = (i == argList.length - 1)? ")":",";
			str = str + argList[i] + end;
		}
		return str;
	}
	@Override
	public boolean equals(Object pred){
		if(!(pred instanceof Predicate))
			return false;
		Predicate predptr = (Predicate)pred;
		if(!this.name.equals(predptr.name))
			return false;
		if(!(this.isNegated == predptr.isNegated))
			return false;
		for(int i=0; i<this.argList.length; ++i){
			if(!this.argList[i].isVariable){
				if(predptr.argList[i].isVariable || !this.argList[i].value.equals(predptr.argList[i].value))
					return false;
			}
			else{
				if(!predptr.argList[i].isVariable)
					return false;
			}
		}
		return true;
	}
	@Override
	public int hashCode(){
		int hash = 7;
		hash = hash + 11*name.hashCode();
		if(isNegated)
			hash++;
		return hash;
	}
}
class Sentence{
	Predicate[] predList;
	HashMap<String, Argument> uniqueArgSet;
	Sentence(Predicate[] predList, HashMap<String, Argument> uniqueArgSet){
		this.predList = predList;
		this.uniqueArgSet = uniqueArgSet;
	}
	public static Comparator<Sentence> senComparator = new Comparator<Sentence>(){
		public int compare(Sentence sen1, Sentence sen2){
			return sen1.predList.length - sen2.predList.length;
		}
	};
	public String toString(){
		String str = "";
		for(int i=0; i<predList.length; ++i){
			String op = (i == predList.length - 1)? "":" | ";
			str = str + predList[i] + op;
		}
		return str;
	}
	@Override
	public boolean equals(Object sen){
		if(!(sen instanceof Sentence))
			return false;
		Sentence senptr = (Sentence)sen;
		if(this.predList.length != senptr.predList.length)
			return false;
		for(int i=0; i<this.predList.length; ++i){
			if(!this.predList[i].equals(senptr.predList[i]))
				return false;
		}
		return true;
	}
	@Override
	public int hashCode(){
		int hash = 7*predList.hashCode();
		hash+=11*uniqueArgSet.hashCode();
		return hash;
	}
}
class PTEntry{
	int senNum;
	int predNum;
	PTEntry(int senNum, int predNum){
		this.senNum = senNum;
		this.predNum = predNum;
	}
	public String toString(){
		return "["+senNum+","+predNum+"] ";
	}
}
class PTable{
	ArrayList<PTEntry> pos;
	ArrayList<PTEntry> neg;
	PTable(){
		pos = new ArrayList<>();
		neg = new ArrayList<>();
	}
	void display(){
		System.out.print("\n\tpos - ");
		Iterator<PTEntry> tab = pos.iterator();
		while(tab.hasNext())
			System.out.print(tab.next());
		System.out.print("\n\tneg - ");
		tab = neg.iterator();
		while(tab.hasNext())
			System.out.print(tab.next());
	}
}
class Soln{
	boolean contr;
	Sentence sentence;
	Soln(boolean contr, Sentence sentence){
		this.contr = contr;
		this.sentence = sentence;
	}
}
class KnowledgeBase{
	ArrayList<Sentence> sentenceList;
	HashMap<String, PTable> table;
	int varCount;
	KnowledgeBase(String[] sentences){
		sentenceList = new ArrayList<>();
		table = new HashMap<>();
		varCount = 0;
		for(int i=0; i<sentences.length; ++i)
			sentenceList.add(makeSentence(sentences[i], i, false));
	}
	Sentence makeSentence(String sentence, int senNum, boolean queryMode){
		String parts[] = sentence.split("[ ]*[|][ ]*");
		Predicate[] predList = new Predicate[parts.length];
		HashMap<String, Argument> uniqueArgSet = null;
		HashMap<String, String> standardizer = new HashMap<>();;
		for(int i=0; i<predList.length; ++i)
			predList[i] = makePredicate(parts[i], senNum, i, uniqueArgSet, standardizer, queryMode);
		Sentence sen = new Sentence(predList, uniqueArgSet);
		return sen;
	}
	Predicate makePredicate(String predicate, int senNum, int predNum, HashMap<String, Argument> uniqueArgSet, HashMap<String, String> standardizer, boolean queryMode){
		String parts[] = predicate.split("\\(");
		StringBuffer predName = new StringBuffer(parts[0]);
		int i=1;
		boolean isNegated;
		if(predName.charAt(0) == '~'){
			predName.deleteCharAt(0);
			if(queryMode)
				isNegated = false;
			else
				isNegated = true;
		}
		else{
			if(queryMode)
				isNegated = true;
			else
				isNegated = false;
		}
		while(predName.charAt(predName.length()-i) == ' ')
			i++;
		predName.delete(predName.length()-i+1, predName.length());
		String name = new String(predName);
		if(parts[1].charAt(parts[1].length()-1) == ')')
			parts[1] = parts[1].substring(0, parts[1].length()-1);
		String arr[] = parts[1].split("[ ]*[,][ ]*");
		Argument[] argList = new Argument[arr.length];
		for(i=0; i<argList.length; ++i){
			if(uniqueArgSet == null)
				uniqueArgSet = new HashMap<>();
			if(arr[i].matches("[a-z]"))
				if(standardizer.get(arr[i]) == null){
					standardizer.put(arr[i], "x"+varCount);
					arr[i] = "x"+varCount;
					varCount++;
				}
				else
					arr[i] = standardizer.get(arr[i]);
			if(uniqueArgSet.get(arr[i]) == null){
				if(arr[i].matches("[A-Z][a-z_A-z]*")){
					Argument arg = new Argument(arr[i], false);
					uniqueArgSet.put(arr[i], arg);
					argList[i] = arg;
				}
				else{
					Argument arg = new Argument(arr[i], true);
					uniqueArgSet.put(arr[i], arg);
					argList[i] = arg;
				}
				
			}
			else{
				argList[i] = uniqueArgSet.get(arr[i]);	
			}
		}
		Predicate pred = new Predicate(name, argList, isNegated);	
		if(table.get(name) == null)
			table.put(name, new PTable());
		if(isNegated)
			table.get(name).neg.add(new PTEntry(senNum, predNum));
		else
			table.get(name).pos.add(new PTEntry(senNum, predNum));
		return pred;	
	}
	void updateTable(Sentence sen, int index){
		for(int p=0; p<sen.predList.length; ++p)
			if(sen.predList[p].isNegated)
				table.get(sen.predList[p].name).neg.add(new PTEntry(index, p));
			else
				table.get(sen.predList[p].name).pos.add(new PTEntry(index, p));	
	}
	void orderedInsert(LinkedList<Sentence> list, Sentence sen){
		int key = sen.predList.length;
		if(list.size() == 0)
			list.add(sen);
		else if(key < list.getFirst().predList.length)
			list.addFirst(sen);
		else if(key >= list.getLast().predList.length)
			list.addLast(sen);
		else{
			int i=0;
			while(key >= list.get(i).predList.length)
				i++;
			list.add(i, sen);
		}
	}
	boolean ask(String query){
		Sentence querySen = makeSentence(query, sentenceList.size(), true);
		sentenceList.add(querySen);
		int sentIndex = sentenceList.size()-1;
		LinkedList<Sentence> orderedList = new LinkedList<>();
		long start = System.nanoTime();
//		display();
		do{
			if(System.nanoTime() - start > 30000000000L)
				return false;
			Sentence sentence = sentenceList.get(sentIndex);
			sentIndex++;
			boolean flag = false;
			for(int i=0; i<sentence.predList.length; ++i){
				Predicate pred = sentence.predList[i];
				ArrayList<PTEntry> list;
//				ArrayList<Sentence> buffer = new ArrayList<>();
				if(pred.isNegated)
					list = table.get(pred.name).pos;
				else
					list = table.get(pred.name).neg;
				for(int j=0; j<list.size(); ++j){
					PTEntry entry = list.get(j);
					Sentence sen2 = sentenceList.get(entry.senNum);
					Soln sen3 = unify(sentence, sen2, i, entry.predNum);
					if(sen3 == null)
					{
//						System.out.println("Cannot unify "+sentence+" and "+sen2);
					}
					else if(sen3.contr == false){
//						System.out.println("Contradiction in resolving "+sentence+" and "+sen2);
						return true;
					}
					else{
						if(!sentenceList.contains(sen3.sentence))// && !buffer.contains(sen3.sentence))
						{
//							buffer.add(sen3.sentence);
							orderedInsert(orderedList, sen3.sentence);
						}
					}
				}
//				for(int k=0; k<buffer.size(); ++k){
//					updateTable(buffer.get(k), sentenceList.size());
//					sentenceList.add(buffer.get(k));
//					flag = true;
//				}
				
			}
			if(orderedList.isEmpty())
				break;
			Sentence sentoadd = orderedList.removeFirst();
			updateTable(sentoadd, sentenceList.size());
			sentenceList.add(sentoadd);
			flag=true;
			//if(flag){
			//	display();
			//	System.out.println(sentIndex);
			//}
		}while(sentIndex < sentenceList.size());
			
//		display();
		return false;
	}
	Soln unify(Sentence sen1, Sentence sen2, int p1, int p2){
		
		Argument[] argList1 = sen1.predList[p1].argList;
		Argument[] argList2 = sen2.predList[p2].argList;
		HashMap<String, String> substitution = new HashMap<>();
		HashMap<String, Argument> newUniqueArgSet = new HashMap<>();
		
		for(int i=0; i<argList1.length; ++i){
			if(!argList1[i].isVariable && !argList2[i].isVariable && !argList1[i].value.equals(argList2[i].value))
				return null;
			else if(argList1[i].isVariable && argList2[i].isVariable){
				if(substitution.get(argList1[i].value) == null){
					substitution.put(argList1[i].value, argList2[i].value);
					newUniqueArgSet.put(argList2[i].value, new Argument("x"+varCount, true));
					varCount++;
				}
				else{
					if(!argList2[i].value.equals(substitution.get(argList1[i].value)))
						return null;
				}
					
			}
			if(argList1[i].isVariable && !argList2[i].isVariable){
				if(substitution.get(argList1[i].value) == null){
					substitution.put(argList1[i].value, argList2[i].value);
					newUniqueArgSet.put(argList2[i].value, new Argument(argList2[i].value, false));
				}
				else{
					if(!argList2[i].value.equals(substitution.get(argList1[i].value)))
						return null;
				}
			}
			else if(argList2[i].isVariable && !argList1[i].isVariable){
				if(substitution.get(argList1[i].value) == null){
					substitution.put(argList2[i].value, argList1[i].value);
					newUniqueArgSet.put(argList1[i].value, new Argument(argList1[i].value, false));
				}
				else{
					if(!argList2[i].value.equals(substitution.get(argList1[i].value)))
						return null;
				}
			}
		}
		
		Predicate[] newPred = new Predicate[sen1.predList.length + sen2.predList.length - 2];
		if(newPred.length == 0){
//			System.out.println("Contradiction arising from resolving "+sen1+"\n"+sen2);
			return new Soln(false, null);
		}
		
//		System.out.println("Resolving "+sen1+" and "+sen2);
		
		int i=0;
		for(int p=0; p<sen1.predList.length; ++p)
			if(p!=p1){
				Predicate temp = sen1.predList[p];
				newPred[i] = new Predicate(temp);
				for(int q=0; q < temp.argList.length; ++q){
					if(substitution.get(temp.argList[q].value) == null){
						if(newUniqueArgSet.get(temp.argList[q].value) != null)
							newPred[i].argList[q] = newUniqueArgSet.get(temp.argList[q].value);
						else{
							if(temp.argList[q].isVariable){
								Argument arg = new Argument("x"+varCount, true);
								varCount++;
								substitution.put(temp.argList[q].value, arg.value);
								newUniqueArgSet.put(arg.value, arg);
								newPred[i].argList[q] = arg;
							}
							else{
								Argument arg = new Argument(temp.argList[q]);
								newUniqueArgSet.put(arg.value, arg);
								newPred[i].argList[q] = arg;
							}
						}
					}
					else{
						newPred[i].argList[q] = newUniqueArgSet.get(substitution.get(temp.argList[q].value));
					}
				}
				i++;
			}
		for(int p=0; p<sen2.predList.length; ++p)
			if(p!=p2){
				Predicate temp = sen2.predList[p];
				newPred[i] = new Predicate(temp);
				for(int q=0; q < temp.argList.length; ++q){
					if(substitution.get(temp.argList[q].value) == null){
						if(newUniqueArgSet.get(temp.argList[q].value) != null)
							newPred[i].argList[q] = newUniqueArgSet.get(temp.argList[q].value);
						else{
							if(temp.argList[q].isVariable){
								Argument arg = new Argument("x"+varCount, true);
								varCount++;
								substitution.put(temp.argList[q].value, arg.value);
								newUniqueArgSet.put(arg.value, arg);
								newPred[i].argList[q] = arg;
							}
							else{
								Argument arg = new Argument(temp.argList[q]);
								newUniqueArgSet.put(arg.value, arg);
								newPred[i].argList[q] = arg;
							}
						}
					}
					else{
						newPred[i].argList[q] = newUniqueArgSet.get(substitution.get(temp.argList[q].value));
					}
				}
				i++;
			}
		Sentence sen3 = new Sentence(newPred, newUniqueArgSet);
		Soln sol = new Soln(true, sen3);
		return sol;
	}
	void display(/*int ptr*/){
		Sentence[] arr = sentenceList.toArray(new Sentence[sentenceList.size()]);
		for(int i=0; i<arr.length; ++i){
//			String end = (i==ptr)?"\t\t<==":"";
			System.out.println(arr[i]/* + end*/);
		}
		System.out.println("-----------");
//		for(Entry<String, PTable> entry: table.entrySet()){
//			System.out.println("\n\n"+entry.getKey());
//			entry.getValue().display();	
//		}
	}
}
public class homework {
	public static void main(String[] args){
		try{
			BufferedReader br = new BufferedReader(new FileReader("input.txt"));
			String[] queries, sentences;
			int n, q;
			q = Integer.parseInt(br.readLine());
			queries = new String[q];
			KnowledgeBase kBases[] = new KnowledgeBase[q];
			for(int i=0; i<q; ++i)
				queries[i] = br.readLine();
			n = Integer.parseInt(br.readLine());
			sentences = new String[n];
			for(int i=0; i<n; ++i)
				sentences[i] = br.readLine();
			br.close();
			BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"));
			for(int i=0; i<q; ++i){
				kBases[i] = new KnowledgeBase(sentences);
				if(kBases[i].ask(queries[i])){
					bw.write("TRUE\n");
					//System.out.println(true);
				}
				else{
					bw.write("FALSE\n");
					//System.out.println(false);
				}
				
			}
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}


