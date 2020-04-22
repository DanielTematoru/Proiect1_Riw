
package Proiect;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;


public class CautareVectoriala {

	//private static HashMap<String,TreeMap<String,Double>>VectorColectie=null;
	private static double Tf(String w,String document) throws IOException
	{
		 File tfFile = new File(document+".tf.json");
	     Type tfFileType = new TypeToken<TreeMap<String, Double>>(){}.getType();
	     Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
	     TreeMap<String, Double> tfFileCollection = gsonBuilder.fromJson(new String(Files.readAllBytes(tfFile.toPath())), tfFileType);
	     return tfFileCollection.get(w);
	}
	
	private static double Idf(String w,Site site) throws IOException
	{
		Type idf_type=new TypeToken<TreeMap<String,Double>>(){}.getType();
		File idf_file=new File(site.getSiteFolder()+"idf.json");
		Gson gson=new GsonBuilder().setPrettyPrinting().create();
		TreeMap<String,Double>idf_colectie=gson.fromJson(new String(Files.readAllBytes(idf_file.toPath())), idf_type);
		if(idf_colectie.containsKey(w))
		{
			return idf_colectie.get(w);
		}
		else
		{
			return 0;
		}
	}
	
	private static double calculTf(String w, ArrayList<String>interogare)
	{
		int nr=0;
		double rez;
		for(String s:interogare)
		{
			if(s.equals(w))
			{
				nr=nr+1;
			}
		}
		rez=(double)nr/interogare.size();
		return rez;
	}
	private static double Cos(TreeMap<String,Double>doc,TreeMap<String,Double>interogare)
	{
		double tf_idf;
		double tf_idf1;
		double numarator=0.0;
		double sum_patrat1=0.0;
		double sum_patrat2=0.0;
		
		for(String w:interogare.keySet())
		{
			if(doc.containsKey(w))
			{
				tf_idf=doc.get(w);
				tf_idf1=interogare.get(w);
				numarator=numarator+Math.abs(tf_idf*tf_idf1);
				sum_patrat1=sum_patrat1+tf_idf*tf_idf;
				sum_patrat2=sum_patrat2+tf_idf1*tf_idf1;
				
			}
		}
		if(numarator==0.0)
		{
			return 0;
		}
		else
		{
			double a=Math.abs(numarator);
			double b=Math.sqrt(sum_patrat1)*Math.sqrt(sum_patrat2);
			double rez=a/b;
			return rez;
		}
		
	}
	public static SortedSet<HashMap.Entry<String,Double>>Cauta(String interogare,Site site,HashMap<String,TreeMap<String,Double>> Vector) throws IOException
	{
		int i=0;
		String[] split=interogare.split(" ");
		ArrayList<String>cuvinte_interogare=new ArrayList<>();
		for(i=0;i<=split.length-1;++i)
		{
			String w=split[i];
			if(ExceptionList.exceptions.contains(w))
			{
				cuvinte_interogare.add(w);
				i++;
			}
			else if(StopWordsList.stopwords.contains(w))
			{
				i++;
			}
			else
			{
				i++;
				PorterStemmer st=new PorterStemmer();
				st.add(w.toCharArray(),w.length());
				st.stem();
				w=st.toString();
				cuvinte_interogare.add(w);
			}
		}
		TreeMap<String,Double>vector=new TreeMap<>();
		for(String w:cuvinte_interogare)
		{
			vector.put(w, calculTf(w, cuvinte_interogare)*Idf(w, site));
			
		}
		HashMap<String,Double> asemanator=new HashMap<>();
		for(String d:Vector.keySet())
		{
			double s=Cos(Vector.get(d), vector);
			if(s!=0)
			{
				asemanator.put(d, s);
			}
		}
		return Sorteaza_Scor(asemanator);
	}
	
	static <k1,k2 extends Comparable<? super k2>>SortedSet<Map.Entry<k1,k2>>Sorteaza_Scor(Map<k1,k2>map){
		
		SortedSet<Map.Entry<k1,k2>>Input=new TreeSet<Map.Entry<k1, k2>>(
				new Comparator<Map.Entry<k1, k2>>() {
					@Override public int compare(Map.Entry<k1, k2>set1,Map.Entry<k1,k2>set2) {
						int rez=set2.getValue().compareTo(set1.getValue());
						if(rez!=0)
						{
							return rez;
						}
						else
						{
							return 1;
						}
					}
					
				}
				);
		Input.addAll(map.entrySet());
		return Input;
		
	}
	public static HashMap<String,TreeMap<String,Double>>Documente(Site site) throws IOException
	{
		int nr_documente=0;
		double tf=0;
		double idf=0;
		
		HashMap<String,TreeMap<String,Double>> doc=new HashMap<>();
		Gson gson= new GsonBuilder().setPrettyPrinting().create();
		String folder=site.getSiteFolder();
		
		File indirect_file=new File(folder+"indirectindex.map");
		Type indirect_type=new TypeToken<HashMap<String,String>>(){}.getType();
		HashMap<String,String>indirect_colectie=gson.fromJson(new String(Files.readAllBytes(indirect_file.toPath())), indirect_type);
		nr_documente=indirect_colectie.keySet().size();
		for(String d:indirect_colectie.keySet())
		{
			File file=new File(indirect_colectie.get(d));
			TreeMap<String,HashMap<String,Integer>>indirect_idx=TF_IDF.Incarca(false, file.getAbsolutePath());
			TreeMap<String,Double>doc_current=new TreeMap<>();
			for(String w:indirect_idx.keySet())
			{
				tf=Tf(w, d);
				idf=Idf(w, site);
				doc_current.put(w, idf*tf);
				
			}
			doc.put(d, doc_current);
			
		}
		Gson gson_doc=new GsonBuilder().setPrettyPrinting().create();
		String vectorfile=gson_doc.toJson(doc);
		Writer doc_writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(folder+"VectorDocumente.json"), "utf-8"));
		doc_writer.write(vectorfile);
		doc_writer.close();
		return doc;
		
		
	}
	
	public static HashMap<String,TreeMap<String,Double>>Incarca_Vector(Site site) throws IOException
	{
		HashMap<String, TreeMap<String,Double>>vector=new HashMap<>();
		JsonReader reader=new JsonReader(new InputStreamReader(new FileInputStream(site.getSiteFolder()+"VectorDocumente.json"),"utf-8"));
		reader.beginObject();
		while(reader.hasNext())
		{
			String doc=reader.nextName();
			TreeMap<String,Double>doc_current=new TreeMap<>();
			
			reader.beginObject();
			while(reader.hasNext())
			{
				doc_current.put(reader.nextName(), reader.nextDouble());
				
			}
			reader.endObject();
			vector.put(doc, doc_current);
			
			
		}
		reader.endObject();
		return vector;
		 		
	}
	
}
