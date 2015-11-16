package br.com.lucene.vetorial;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.util.Version;

public class Buscador {
	private static Logger logger = Logger.getLogger(Buscador.class);

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
	
	// Delimitador dos termos das strings dos �ndices e consulta
	private String delimitadorSeparacao;

	public Buscador(Directory dirIndiceEmMemoria, Analyzer analisador, Similarity similaridade, String delimitadorSeparacao) {
		this.dirIndiceEmMemoria = dirIndiceEmMemoria;
		this.analisador = analisador;
		this.similaridade = similaridade;
		this.delimitadorSeparacao = delimitadorSeparacao;
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
		Query query = parser.parse(delimitarTermos(conteudoConsulta));

		// Adiciona a consulta � consulta geral
		bQuery.add(query, Occur.MUST);
		return bQuery;
	}

	// Separa os termos da string pelo delimitador de separa��o e envolve-os com aspas ("")
	// M�todo criado para que tab, LF, CR sejam considerados
	public String delimitarTermos(String conteudoConsulta) {
		// Escapa os caracteres especiais
		conteudoConsulta = QueryParser.escape(conteudoConsulta);
		
		// Delimita��o dos termos da consulta
		String termos[] = conteudoConsulta.split(delimitadorSeparacao);
		StringBuffer conteudoConsultaDelimitada = new StringBuffer();
		for (String termo : termos) {
			termo = "\"" + termo + "\"";
			conteudoConsultaDelimitada.append(termo);
		}
		
		return conteudoConsultaDelimitada.toString();
	}

}
