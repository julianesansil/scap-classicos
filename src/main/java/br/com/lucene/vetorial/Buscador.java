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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
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

	// Representa a fun��o de similaridade usada na busca
	private Similarity similaridade;

	public Buscador(File dirIndice, Similarity similaridade) {
		logger.info("Diretorio do �ndice: " + dirIndice.getAbsolutePath());
		this.dirIndice = dirIndice;
		this.similaridade = similaridade;
		this.configurarBuscador();
	}

	public void configurarBuscador() {
		try {
			dirIndiceEmMemoria = new SimpleFSDirectory(dirIndice);
			// A separa��o dos termos � feita atrav�s dos espa�os em branco do texto
			analisador = new WhitespaceAnalyzer(Version.LUCENE_48);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public String buscar(File arquivoConsulta) {
		String conteudoConsulta = null;

		try {
			conteudoConsulta = new String(Files.readAllBytes(arquivoConsulta.toPath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error(e);
		}

		return buscar(conteudoConsulta);
	}

	public String buscar(String conteudoConsulta) {
		String autorScap = "";

		try {
			leitor = DirectoryReader.open(dirIndiceEmMemoria);
			buscador = new IndexSearcher(leitor);
			buscador.setSimilarity(similaridade);

			Query query = criarQuery(conteudoConsulta);
			// Realiza a busca e armazena o resultado em um TopDocs
			TopDocs resultado = buscador.search(query, 1);

			// Representa cada um dos documentos retornados na busca
			for (ScoreDoc sd : resultado.scoreDocs) {
				Document documento = buscador.doc(sd.doc);
				autorScap = documento.get("autorArquivo");
				// logger.info("Pontua��o: " + sd.score);
			}

			leitor.close();
		} catch (Exception e) {
			logger.error(e);
		}

		return autorScap;
	}

	public Query criarQuery(String conteudoConsulta) throws ParseException {
		BooleanQuery bQuery = new BooleanQuery();
		BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
		// Guarda todas possibilidades de combina��es de termos dos arquivos
		bQuery.add(new MatchAllDocsQuery(), Occur.SHOULD);

		// Representa o conte�do a ser consultado (�ndice)
		QueryParser parser = new QueryParser(Version.LUCENE_48, "conteudoIndice", analisador);
		// Representa a consulta do usu�rio
		conteudoConsulta = QueryParser.escape(conteudoConsulta);
		Query query = parser.parse(conteudoConsulta);

		// Adiciona a consulta � consulta geral
		bQuery.add(query, Occur.MUST);
		return bQuery;
	}

}
