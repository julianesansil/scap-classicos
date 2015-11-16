package br.com.lucene.main;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PatternAnalyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import br.com.lucene.Util;
import br.com.lucene.vetorial.Buscador;
import br.com.lucene.vetorial.Experimento;
import br.com.lucene.vetorial.Indexador;
import br.com.lucene.vetorial.Preparador;

@SuppressWarnings("deprecation")
public class MainExperimento {
	private static Logger logger = Logger.getLogger(Main.class);

	// Diret�rio dos arquivos a serem preparados
	public static final File DIR_BASE = new File("S:/Dropbox/TCC/Codigo/util/n-grams/");

	// Diret�rio dos arquivos a serem indexados
	public static final File DIR_BASE_PREPARADA = new File("S:/Dropbox/TCC/Codigo/util/n-grams-autor/");

	// Diret�rio de armazenamento do �ndice
	public static final File DIR_INDICE = new File("S:/Dropbox/TCC/Codigo/util/indices/");

	// Representa o diret�rio do �ndice em mem�ria
	private static Directory dirIndiceEmMemoria;

	// Respons�vel pelo pr�-processamento do texto
	private static Analyzer analisador;

	// Delimitador que indica onde come�a e/ou termina os termos do �ndice e da consulta
	private static final String DELIMITADOR_SEPARACAO = " ";
	private static final String EXTENSAO_ACEITA = ".txt";

	private static Preparador preparador;
	private static Indexador indexador;
	private static Buscador buscador;

	public static void main(String[] args) {
		long inicio = System.currentTimeMillis();
		configurarProcessamento();
		iniciarVariaveis();

		logger.info("IN�CIO DA PREPARA��O");
		logger.info("**********************************************************************************");

		preparar();

		logger.info("**********************************************************************************");
		logger.info("FIM DA PREPARA��O\n");

		logger.info("IN�CIO DA BUSCA");
		logger.info("**********************************************************************************");

		testar();

		logger.info("**********************************************************************************");
		logger.info("FIM DA BUSCA\n");

		long fim = System.currentTimeMillis();
		logger.info("Tempo de execu��o: " + ((fim - inicio) / 1000) + "s");
	}

	public static void iniciarVariaveis() {
		preparador = new Preparador(DIR_BASE_PREPARADA, DELIMITADOR_SEPARACAO);
		indexador = new Indexador(DIR_INDICE, dirIndiceEmMemoria, analisador);

		// Fun��o de similaridade escolhida = Vetorial
		Similarity similaridade = new DefaultSimilarity();
		// Fun��o de similaridade escolhida = Okapi BM25
		// Similarity similaridade = new BM25Similarity();

		buscador = new Buscador(dirIndiceEmMemoria, analisador, similaridade, DELIMITADOR_SEPARACAO);
	}

	public static void configurarProcessamento() {
		logger.info("Diretorio do �ndice: " + DIR_INDICE.getAbsolutePath());

		try {
			dirIndiceEmMemoria = new SimpleFSDirectory(DIR_INDICE);
			// A separa��o dos termos � feita atrav�s do DELIMITADOR_SEPARACAO
			analisador = new PatternAnalyzer(Version.LUCENE_48, Pattern.compile(DELIMITADOR_SEPARACAO), false, null);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public static void preparar() {
		Util.esvaziarDiretorio(DIR_BASE_PREPARADA);
		preparador.prepararDiretorio(DIR_BASE, "", EXTENSAO_ACEITA, null);
	}

	public static void testar() {
		Experimento experimento = new Experimento(preparador, indexador, buscador);
		experimento.testar(DIR_BASE, DIR_BASE_PREPARADA, EXTENSAO_ACEITA);
	}

}
