package Proiect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

public class Cautare_Booleana {

	
	private static HashSet<String>AplicaOperator(Set<String>operand1,Set<String>operand2,String operator)
	{
		HashSet<String> rezultat=new HashSet<>();
		Set<String> primulSet;
		Set<String> setDoi;
		boolean primulSet_Mic=false;
		
		switch(operator.toLowerCase())
		{
		case "&"://functia si
			primulSet_Mic=(operand1.size()<operand2.size());
			primulSet=(primulSet_Mic)?operand1:operand2;
			setDoi=(primulSet_Mic)?operand2:operand1;
			for(String doc:primulSet)
			{
				if(setDoi.contains(doc))
				{
					rezultat.add(doc);
				}
			}
			return rezultat;
		case "||"://functia sau
			primulSet_Mic=(operand1.size()<operand2.size());
			primulSet=(primulSet_Mic)?operand2:operand1;
			setDoi=(primulSet_Mic)?operand1:operand2;
			rezultat.addAll(primulSet);
			for(String doc:setDoi)
			{
				if(!rezultat.contains(doc))
				{
					rezultat.add(doc);	
				}
			}
			return rezultat;
		case "^"://functia not
			for(String doc:operand1)
			{
				if(!operand2.contains(doc))
				{
					rezultat.add(doc);
				}
			}
			return rezultat;
		default:
			return null;
			
		}
	}
	
	public static Set<String> Cautare_booleana(String request,TreeMap<String,HashMap<String,Integer>> indirect_index)
	{
		Stack<String> operator=new Stack<>();
		Stack<String>operand=new Stack<>();
		String[] Spargere=request.split(" ");//spargem dupa spatiu
		int n;
		n=Spargere.length-1;
		for(int i=n;i>=0;--i)
		{
			String w=Spargere[i];
			if(ExceptionList.exceptions.contains(w))
			{
				operand.push(w);
				i=i-1;
				if(i>=0)
				{
					operator.push(Spargere[i--]);
				}
				
			}
			else if(StopWordsList.stopwords.contains(w))
			{
				i=i-2;
			}
			else
			{
				operand.push(w);
				i=i-1;
				if(i>=0)
				{
					operator.push(Spargere[i--]);
				}
			}
			
		}
		Set<String> rez=CautareW(indirect_index,operand.pop());
		
		while(!operand.empty()&&!operator.empty())
		{
			String op=operand.pop();
			String or=operator.pop();
			Set<String>current=CautareW(indirect_index,op);
			rez=AplicaOperator(rez, current, or);
		}
		return rez;
		
		
	}
	private static Set<String> CautareW(TreeMap<String,HashMap<String,Integer>>indirect_index,String W)
	{
		if(indirect_index.containsKey(W))
		{
			return indirect_index.get(W).keySet();
		}
		else
		{
			return null;
		}
		
	}
}
