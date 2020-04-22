package Proiect;
import java.io.IOException;
import java.util.*;

public class Proiect1 {

	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		Site site=new Site("E:\\Eclipse\\New folder\\Proiect_01_RIW\\stackoverflow/", "https://stackoverflow.com/");
		System.out.print("Creare index direct...\n");
		
		HashMap<String, HashMap<String, Integer>> directIndex = TF_IDF.directIndex(site);
		System.out.print("-------------------------------------------------------------------------\n");
		System.out.print("Creare index indirect...\n\n");
        TreeMap<String, HashMap<String, Integer>> indirectIndex = TF_IDF.indirectIndex(site);
        System.out.print("-------------------------------------------------------------------------\n");
		System.out.print("Incarcare index indirect din memorie...\n");
		indirectIndex=TF_IDF.Incarca(true, site.getSiteFolder()+"indirectindex.json");
		System.out.print("-------------------------------------------------------------------------\n");
		
        System.out.print("Aplicare cautare booleana\nIntroduceti ce vreti sa cautati:\n");
        Scanner b=new Scanner(System.in);
        String interog_booleana=b.nextLine();
        System.out.print("-------------------------------------------------------------------------\n");
        System.out.println("Incepe cautarea.");
        Set<String>rez_boolean=Cautare_Booleana.Cautare_booleana(interog_booleana, indirectIndex);
        if(rez_boolean==null)
        {
        	System.out.println("Nu s-a gasit niciun rezultat\n");
        }
        else
        {
        	System.out.println("Rezultatele gasite din cautarea booleana  sunt:\n");
        	for(String doc:rez_boolean)
        	{
        		System.out.print("\t"+doc+"\n");
        	}
        }
        System.out.print("-------------------------------------------------------------------------\n");
		System.out.print("Se creeaza vectorii asociati elementelor html...\n");
		System.out.print("-------------------------------------------------------------------------\n");
		HashMap<String,TreeMap<String,Double>>Vectori=CautareVectoriala.Documente(site);
		System.out.print("-------------------------------------------------------------------------\n");
		System.out.print("Incarca vectorii asociati elementelor html...\n");
		System.out.print("-------------------------------------------------------------------------\n");
		Vectori=CautareVectoriala.Incarca_Vector(site);
		SortedSet<HashMap.Entry<String,Double>>rezCautareVectoriala;
		System.out.print("Aplicare cautare vectoriala\nIntroduceti ce vreti sa cautati:\n");
        Scanner v=new Scanner(System.in);
        String interog=v.nextLine();
        System.out.print("-------------------------------------------------------------------------\n");
        System.out.println("Incepe cautarea.");
        rezCautareVectoriala=CautareVectoriala.Cauta(interog, site, Vectori);
        if(rezCautareVectoriala==null)
        {
        	System.out.println("Nu s-a gasit niciun rezultat\n");
        }
        else
        {
        	System.out.println("Rezultatele gasite din cautarea vectoriala sunt:\n");
        	for(Map.Entry<String,Double>doc:rezCautareVectoriala)
        	{
        		System.out.print("\t"+doc+"\n");
        	}
        }

	}

}
