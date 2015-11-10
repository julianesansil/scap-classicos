package br.com.lucene.vetorial;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class Buscador {
	private static Logger logger = Logger.getLogger(Buscador.class);

	// Diret�rio de armazenamento do �ndice
	private File dirIndice;

	// Representa o diret�rio do �ndice em mem�ria
	private Directory dirIndiceEmMemoria;

	// Respons�vel pelo pr�-processamento do texto
	private Analyzer analisador;

	// Classe abstrata que acessa o �ndice
	private IndexReader leitor;

	// Implementa os m�todos necess�rios para realizar buscas num �ndice
	private IndexSearcher buscador;

	
	public Buscador(File dirIndice) {
		this.dirIndice = dirIndice;
		this.configurarBuscador();
	}

	public void configurarBuscador() {
		try {
			logger.info("Diretorio do �ndice: " + dirIndice.getAbsolutePath());
			dirIndiceEmMemoria = new SimpleFSDirectory(dirIndice);

			// A separa��o dos termos � feita atrav�s dos espa�os em branco do texto
			analisador = new WhitespaceAnalyzer(Version.LUCENE_48);

			leitor = DirectoryReader.open(dirIndiceEmMemoria);
			buscador = new IndexSearcher(leitor);

		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public String buscar(File arquivo) {
		String conteudo = null;
		
		try {
			conteudo = new String(Files.readAllBytes(arquivo.toPath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error(e);
		}

		return buscar(conteudo);
	}

	public String buscar(String conteudo) {
		String autorScap = "";
		
		try {
			// Representa a consulta do usu�rio
			QueryParser parser = new QueryParser(Version.LUCENE_48, "Texto", analisador);
			conteudo = QueryParser.escape(conteudo);
			Query consulta = parser.parse(conteudo);
			
			// Realiza a busca e armazena o resultado em um TopDocs
			TopDocs resultado = buscador.search(consulta, 5);
			
			// Representa cada um dos documentos retornados na busca
			for (ScoreDoc sd : resultado.scoreDocs) {
				Document documento = buscador.doc(sd.doc);
//				autorScap = documento.get("Caminho");
//				logger.info("Caminho: " + documento.get("Caminho"));
//				logger.info("Pontua��o: " + sd.score);
			}

			leitor.close();
		} catch (Exception e) {
			logger.error(e);
		}
		
		return autorScap;
	}
	
}
