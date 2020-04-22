package Proiect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;

public class TF_IDF {
	   private String websiteFolder;
	   private String baseUri;
	   
	   TF_IDF(String websiteFolder, String baseUri)
	    {
	        this.websiteFolder = websiteFolder;
	        this.baseUri = baseUri;
	    }
	   public String getTitle(Document doc) // preia titlul documentului
	    {
	        String title = doc.title();
	        // System.out.println("Titlul site-ului: " + title);
	        return title;
	    }

	    private String getKeywords(Document doc) // preia cuvintele cheie
	    {
	        Element keywords = doc.selectFirst("meta[name=keywords]");
	        String keywordsString = "";
	        if (keywords == null) {
	             //System.out.println("Nu exista tag-ul <meta name=\"keywords\">!");
	        } else {
	           keywordsString = keywords.attr("content");
	            //System.out.println("Cuvintele cheie au fost preluate!");
	        }
	        return keywordsString;
	    }

	    private String getDescription(Document doc) // preia descrierea site-ului
	    {
	        Element description = doc.selectFirst("meta[name=description]");
	        String descriptionString = "";
	        if (description == null) {
	            // System.out.println("Nu exista tag-ul <meta name=\"description\">!");
	        } else {
	            descriptionString = description.attr("content");
	           // System.out.println("Descrierea site-ului a fost preluata!");
	        }
	        return descriptionString;
	    }

	    private String getRobots(Document doc) // preia lista de robots
	    {
	        Element robots = doc.selectFirst("meta[name=robots]");
	        String robotsString = "";
	        if (robots == null) {
	            System.out.println("Nu exista tag-ul <meta name=\"robots\">!");
	        } else {
	            robotsString = robots.attr("content");
	            // System.out.println("Lista de robots a site-ului a fost preluata!");
	        }
	        return robotsString;
	    }

	    private Set<String> getLinks(Document doc) throws IOException // preia link-urile de pe site (ancorele)
	    {
	        Elements links = doc.select("a[href]");
	        Set<String> URLs = new HashSet<String>();
	        for (Element link : links) {
	            String absoluteLink = link.attr("abs:href"); // facem link-urile relative sa fie absolute
	            if (absoluteLink.contains(baseUri)) // ignoram legaturile interne
	            {
	                continue;
	            }

	            // cautam eventuale ancore in link-uri
	            int anchorPosition = absoluteLink.indexOf('#');
	            if (anchorPosition != -1) // daca exista o ancora (un #)
	            {
	                // stergem partea cu ancora din link
	                StringBuilder tempLink = new StringBuilder(absoluteLink);
	                tempLink.replace(anchorPosition, tempLink.length(), "");
	                absoluteLink = tempLink.toString();
	            }

	            // nu vrem sa adaugam duplicate, asa incat folosim o colectie de tip Set
	            URLs.add(absoluteLink);
	        }
	        // System.out.println("Link-urile de pe site au fost preluate!");
	        return URLs;
	    }

	    public static  File getTextFromHTML(Site site,Document doc, File html) throws IOException // preia textul din document si il pune intr-un fisier
	    {
	        StringBuilder sb = new StringBuilder();
	        sb.append(site.getTitle(doc)); // titlul
	        sb.append(System.lineSeparator());
	        sb.append(site.getKeywords(doc)); // cuvintele cheie
	        sb.append(System.lineSeparator());
	        sb.append(site.getDescription(doc));
	        sb.append(System.lineSeparator());
	        sb.append(doc.body().text());
	        String text = sb.toString();

	       
	        StringBuilder textFileNameBuilder = new StringBuilder(html.getAbsolutePath());

	        textFileNameBuilder.append(".txt");
	        
	        String textFileName = textFileNameBuilder.toString();

	        
	        FileWriter fw = new FileWriter(new File(textFileName), false);
	        fw.write(text);
	        fw.close();

	        return new File(textFileName);
	    }
	    
	    private static HashMap<String,Integer> processText(String fileName) throws IOException
	    {
	    	TreeMap<String,Double>Lista_tf=new TreeMap<>();
	    	int Nr_cuvinte_Doc=0;
	    	HashMap<String,Integer> wordList=new HashMap<String,Integer>();
	    	FileReader inputStream = null;
	        inputStream = new FileReader(fileName);
	        
	        StringBuilder sb=new StringBuilder();
	        int c;
	        while ((c = inputStream.read()) != -1)
	        {
	            
	            if (!Character.isLetterOrDigit((char)c)) // suntem pe un separator
	            {
	                String newWord = sb.toString(); // cream cuvantul nou

	              
	                if (ExceptionList.exceptions.contains(newWord))
	                {
	                    
	                    if (wordList.containsKey(newWord)) 
	                    {
	                        wordList.put(newWord, wordList.get(newWord) + 1); 
	                    } else 
	                    {
	                        wordList.put(newWord, 1);
	                    }
	                    ++Nr_cuvinte_Doc;
	                }
	               
	                else if (StopWordsList.stopwords.contains(newWord))
	                {
	                   
	                    sb.setLength(0);
	                    continue;
	                }
	                else 
	                {
	                	PorterStemmer st=new PorterStemmer();
	                	st.add(newWord.toCharArray(),newWord.length());
	                	st.stem();
	                	newWord=st.toString();
	                    
	                    if (wordList.containsKey(newWord)) 
	                    {
	                        wordList.put(newWord, wordList.get(newWord) + 1); 
	                    } else // daca nu, il adaugam
	                    {
	                        wordList.put(newWord, 1);
	                    }
	                    ++Nr_cuvinte_Doc;
	                }

	                // System.out.println(newWord + " -> " + hashText.get(newWord));
	                sb.setLength(0); 
	            }
	            else 
	            {
	                sb.append((char)c); 
	            }
	        }

	        
	        wordList.remove("");

	        
	        StringBuilder sbDirectIndexFileName = new StringBuilder(fileName);

	        
	        sbDirectIndexFileName.replace(sbDirectIndexFileName.lastIndexOf(".") + 1, sbDirectIndexFileName.length(), "directindex.json");
	        Writer writer = new BufferedWriter(new OutputStreamWriter(
	                new FileOutputStream(sbDirectIndexFileName.toString()), "utf-8"));

	        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
	        String jsonFile = gsonBuilder.toJson(wordList);

	        writer.write(jsonFile);
	        writer.close();

	        inputStream.close();
	        // System.out.println("Cuvintele din textul de pe site au fost prelucrate!");

	        //return wordList;
	        for(String w:wordList.keySet())
	        {
	        	Lista_tf.put(w, (double)wordList.get(w)/Nr_cuvinte_Doc);
	        }
	        StringBuilder TFName=new StringBuilder(fileName);
	        TFName.replace(TFName.lastIndexOf(".")+1,TFName.length() ,"tf.json");
	        //Gson TFGson=new GsonBuilder().setPrettyPrinting().create();
	        Writer tf=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(TFName.toString()),"utf-8"));
	        Gson TFGson=new GsonBuilder().setPrettyPrinting().create();
	        String tf_json=TFGson.toJson(Lista_tf);
	        tf.write(tf_json);
	        tf.close();
	        return wordList;
	    }
	    
	    public static  HashMap<String, HashMap<String, Integer>> directIndex(Site site) throws IOException
	    {
	    	String websiteFolder=site.getSiteFolder();
	    	String baseUri=site.getUri();
	    	
	        HashMap<String, HashMap<String, Integer>> directIndex = new HashMap<>();
	        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
	        
	        Writer mapFileWriter = new BufferedWriter(new OutputStreamWriter(
	        new FileOutputStream(websiteFolder + "directindex.idx"), "utf-8"));
	        HashMap<String, String> mapFile = new HashMap<>();

	        LinkedList<String> folderQueue = new LinkedList<>();

	        folderQueue.add(websiteFolder);
	       
	        while (!folderQueue.isEmpty()) 
	        {
	           
	            String currentFolder = folderQueue.pop();
	            File folder = new File(currentFolder);
	            File[] listOfFiles = folder.listFiles();
	            try {
	                for (File file : listOfFiles)
	                {
	                    
	                    if (file.isFile() && Files.probeContentType(file.toPath()).endsWith(".txt"))
	                    {
	                       
	                        Document doc = Jsoup.parse(file, null, baseUri);
	                        String fileName = file.getAbsolutePath();
	                       // System.out.println("Am parsat fisierul HTML \"" + fileName + "\".");
	                        
	                        File textFile = getTextFromHTML(site,doc, file);
	                        String textFileName = textFile.getAbsolutePath();
	                        
	                      //  System.out.println(" Am preluat textul din fisierul HTML \"" + fileName + "\".");

	                        
	                        HashMap<String, Integer> currentDocWords = processText(textFileName);
	                        

	                        
	                        directIndex.put(fileName, currentDocWords);

	                       
	                        mapFile.put(fileName, fileName + ".directindex.json");

	                       // System.out.println(" Am creat index-ul direct din fisierul TEXT \"" + textFileName + "\".");
	                    }
	                    else if (file.isDirectory()) // daca este folder, il punem in coada
	                    {
	                    	
	                        folderQueue.add(file.getAbsolutePath());
	                    }
	                }
	            } catch (NullPointerException e) {
	                //System.out.println("Nu exista fisiere in folderul \"" + currentFolder + "\"!");
	            }
	        }

	      
	        mapFileWriter.write(gsonBuilder.toJson(mapFile));
	        
	        mapFileWriter.close();
	       // System.out.println(System.lineSeparator());
	       // System.out.println("Fisierul de mapare \"" + websiteFolder + "directindex.idx\".");
	        
	        return directIndex;
	    }
	    
	    public static  TreeMap<String, HashMap<String, Integer>> indirectIndex (Site site ) throws IOException
	    {
	        TreeMap<String, HashMap<String, Integer>> indirectIndex = new TreeMap<>();
	        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
	        TreeMap<String,Double>idf=new TreeMap<>();
	        int Nr_Doc=0;
	        String websiteFolder=site.getSiteFolder();
	        
	        Writer mapFileWriter = new BufferedWriter(new OutputStreamWriter(
	                new FileOutputStream(websiteFolder + "indirectindex.map"), "utf-8"));
	        HashMap<String, String> mapFile = new HashMap<>();
	        LinkedList<String> folderQueue = new LinkedList<>();

	        folderQueue.add(websiteFolder);

	        while (!folderQueue.isEmpty()) 
	        {
	           
	            String currentFolder = folderQueue.pop();
	            File folder = new File(currentFolder);
	            File[] listOfFiles = folder.listFiles();

	            //  parcurgem lista de fisiere / foldere
	            try {
	                for (File file : listOfFiles)
	                {
	                    
	                    if (file.isFile() && file.getAbsolutePath().endsWith(".directindex.json"))
	                    {
	                        String fileName = file.getAbsolutePath();
	                        String docName = fileName.replace(".directindex.json", "");
	                        
	                        Type directIndexType = new TypeToken<HashMap<String, Integer>>(){}.getType();
	                        HashMap<String, Integer> directIndex = gsonBuilder.fromJson(new String(Files.readAllBytes(file.toPath())), directIndexType);
	                      //  System.out.println(" Am parsat fisierul JSON \"" + fileName + "\".");

	                        TreeMap<String, HashMap<String, Integer>> localIndirectIndex = new TreeMap<>();
	                        for(Map.Entry<String, Integer> entry : directIndex.entrySet()) 
	                        {
	                            String word = entry.getKey();
	                            int numberOfApparitions = entry.getValue();
                            
	                            if (localIndirectIndex.containsKey(word))
	                            {	                              
	                                HashMap<String, Integer> apparitions = localIndirectIndex.get(word);
	                                apparitions.put(docName, numberOfApparitions);
	                               
	                            }
	                            else
	                            {
	                                HashMap<String, Integer> apparitions = new HashMap<>();
	                                apparitions.put(docName, numberOfApparitions);
	                                localIndirectIndex.put(word, apparitions);
	                            }
	                            
	                            if (indirectIndex.containsKey(word)) 
	                            {
	                               
	                                HashMap<String, Integer> apparitions = indirectIndex.get(word);
	                                apparitions.put(docName, numberOfApparitions);
	                                
	                            }
	                            else
	                            {
	                                HashMap<String, Integer> apparitions = new HashMap<>();
	                                apparitions.put(docName, numberOfApparitions);
	                                indirectIndex.put(word, apparitions);
	                            }
	                        }

	                        
	                        Writer writer = new BufferedWriter(new OutputStreamWriter(
	                                new FileOutputStream(docName + ".indirectindex.json"), "utf-8"));
	                        writer.write(gsonBuilder.toJson(localIndirectIndex));
	                        writer.close();

	                      //  System.out.println(" Index-ul indirect in fisierul JSON \"" + docName + ".indirectindex.json\".");

	                        ++Nr_Doc;
	                        mapFile.put(docName, docName + ".indirectindex.json");
	                       
	                    }
	                    else if (file.isDirectory()) 
	                    {
	                        folderQueue.add(file.getAbsolutePath());
	                    }
	                }
	            } catch (NullPointerException e) {
	                //System.out.println("Nu exista fisiere in folderul \"" + currentFolder + "\"!");
	            }
	        }

	       
	        Writer indirectIndexWriter = new BufferedWriter(new OutputStreamWriter(
	                new FileOutputStream(websiteFolder + "indirectindex.json"), "utf-8"));
	        indirectIndexWriter.write(gsonBuilder.toJson(indirectIndex));
	        indirectIndexWriter.close();
	      //  System.out.println(System.lineSeparator());
	      //  System.out.println("Fisierul de index indirect \"" + websiteFolder + "indirectindex.json\".");       
	        mapFileWriter.write(gsonBuilder.toJson(mapFile));
	        mapFileWriter.close();
	        //System.out.println(" Fisierul de mapare \"" + websiteFolder + "indirectindex.map\".");
	        
	        for(String w:indirectIndex.keySet())
	        {
	        	int current=indirectIndex.get(w).size();
	        	idf.put(w, Math.log((double)Nr_Doc/current));
	        	
	        }
	        Writer idfWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(websiteFolder+"idf.json"),"utf-8"));
	        idfWriter.write(gsonBuilder.toJson(idf));
	        idfWriter.close();
	        
	        return indirectIndex;
	    }
	    private static TreeMap<String, HashMap<String, Integer>> Grup_index = null;
	    private static boolean verificat = false;
	    
	   
	    
	    public static TreeMap<String,HashMap<String,Integer>>Incarca(boolean ok,String File)throws IOException
	    {
	    	if(verificat&&ok)
	    	{
	    		return Grup_index;
	    	}
	    	TreeMap<String,HashMap<String,Integer>>indirect_idx=new TreeMap<>();
	    	JsonReader r=new JsonReader(new InputStreamReader(new FileInputStream(File),"UTF-8"));
	    	
	    	r.beginObject();
	    	while(r.hasNext())
	    	{
	    		String w=r.nextName();
	    		HashMap<String,Integer>current=new HashMap<>();
	    		r.beginObject();
	    		while(r.hasNext())
	    		{
	    			current.put(r.nextName(), r.nextInt());
	    		}
	    		r.endObject();
	    		indirect_idx.put(w, current);
	    		
	    	}
	    	r.endObject();
	    	verificat=true;
	    	Grup_index=indirect_idx;
	    	return indirect_idx;
	    }
	    
	    public void getWordsFromTextFiles() throws IOException
	    {
	        LinkedList<String> folderQueue = new LinkedList<>();

	        folderQueue.add(websiteFolder);

	        while (!folderQueue.isEmpty()) // cat timp nu mai sunt foldere copil de parcurs
	        {
	            
	            String currentFolder = folderQueue.pop();
	            File folder = new File(currentFolder);
	            File[] listOfFiles = folder.listFiles();

	            
	            try {
	                for (int i = 0; i < listOfFiles.length; i++)
	                {
	                    File file = listOfFiles[i];

	                    if (file.isFile() && file.getAbsolutePath().endsWith(".txt"))
	                    {
	                        processText(file.getAbsolutePath());
	                       // System.out.println("Am procesat fisierul TEXT \"" + file.getAbsolutePath() + "\".");
	                    }
	                    else if (file.isDirectory())
	                    {
	                        folderQueue.add(file.getAbsolutePath());
	                    }
	                }
	            } catch (NullPointerException e) {
	                //System.out.println("Nu exista fisiere in folderul \"" + currentFolder + "\"!");
	            }
	        }
	    }
	    
}

