package br.com.lucene.main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import br.com.lucene.Util;
import br.com.lucene.vetorial.Buscador;
import br.com.lucene.vetorial.Indexador;
import br.com.lucene.vetorial.Preparador;

public class Main {
	private static Logger logger = Logger.getLogger(Main.class);

	// Diret�rio dos arquivos a serem preparados
	public static final String DIR_BASE = "S:/Dropbox/TCC/Codigo/util/n-grams/";

	// Diret�rio dos arquivos a serem indexados
	public static final String DIR_BASE_PREPARADA = "S:/Dropbox/TCC/Codigo/util/vetorial-bm25/n-grams-autor/";

	// Diret�rio dos arquivos-consultas
	public static final String DIR_ARQUIVOS_CONSULTA = "S:/Dropbox/TCC/Codigo/util/vetorial-bm25/vetorial-bm25/arquivos-consulta/";
	
	// Diret�rio de armazenamento do �ndice
	public static final String DIR_INDICE = "S:/Dropbox/TCC/Codigo/util/indices/";
	
	private static final String EXTENSAO_ACEITA = ".txt";

	private static Preparador preparador;
	private static Indexador indexador;
	private static Buscador buscador;

	public static void main(String[] args) {
		long inicio = System.currentTimeMillis();
		iniciarVariaveis();

		logger.info("IN�CIO DA PREPARA��O");
		logger.info("**********************************************************************************");

		preparar();

		logger.info("**********************************************************************************");
		logger.info("FIM DA PREPARA��O\n");

		logger.info("IN�CIO DA INDEXA��O");
		logger.info("**********************************************************************************");

		indexar();

		logger.info("**********************************************************************************");
		logger.info("FIM DA INDEXA��O\n");

		logger.info("IN�CIO DA BUSCA");
		logger.info("**********************************************************************************");

		buscar();

		logger.info("**********************************************************************************");
		logger.info("FIM DA BUSCA\n");

		long fim = System.currentTimeMillis();
		logger.info("Tempo de execu��o: " + ((fim - inicio) / 1000) + "s");
	}

	public static void iniciarVariaveis() {
		preparador = new Preparador(new File(DIR_BASE_PREPARADA));
		indexador = new Indexador(new File(DIR_INDICE));

		// Fun��o de similaridade escolhida = Vetorial
		Similarity similaridade = new DefaultSimilarity();
		// Fun��o de similaridade escolhida = Okapi BM25
		//Similarity similaridade = new BM25Similarity();

		buscador = new Buscador(new File(DIR_INDICE), similaridade);
	}

	public static void preparar() {
		Util.esvaziarDiretorio(new File(DIR_BASE_PREPARADA));
		preparador.prepararDiretorio(new File(DIR_BASE), "", EXTENSAO_ACEITA, null);
	}

	public static void indexar() {
		indexador.indexarDiretorio(new File(DIR_BASE_PREPARADA), EXTENSAO_ACEITA);
	}

	public static void buscar() {
		String autorScap = "";

		Path pathArquivo = Paths.get("S:/Dropbox/TCC/Codigo/util/", "teste.txt");
		String conteudoConsulta = null;
		try {
			conteudoConsulta = new String(Files.readAllBytes(pathArquivo), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("N�o foi poss�vel fazer a leitura do arquivo");
		}

		autorScap = buscador.buscar(conteudoConsulta);
		logger.info("Autor: " + autorScap);
	}

}
