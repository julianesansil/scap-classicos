package br.com.lucene.main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

import br.com.lucene.vetorial.Buscador;
import br.com.lucene.vetorial.Experimento;
import br.com.lucene.vetorial.Indexador;
import br.com.lucene.vetorial.Preparador;

public class MainExperimento {
	private static Logger logger = Logger.getLogger(Main.class);

	// Diretório dos arquivos a serem preparados
	public static final String DIR_BASE = "S:/Dropbox/TCC/Codigo/util/n-grams/";

	// Diretório dos arquivos a serem indexados
	public static final String DIR_BASE_PREPARADA = "S:/Dropbox/TCC/Codigo/util/n-grams-autor/";

	// Diretório de armazenamento do índice
	public static final String DIR_INDICE ="S:/Dropbox/TCC/Codigo/util/indices/";

	private static final String EXTENSAO_ACEITA = ".txt";

	private static Preparador preparador;
	private static Indexador indexador;
	private static Buscador buscador;

	public static void main(String[] args) {
		long inicio = System.currentTimeMillis();

		logger.info("**********************************************************************************");
		logger.info("INÍCIO DA PREPARAÇÃO");

		preparar();

		logger.info("FIM DA PREPARAÇÃO");
		logger.info("**********************************************************************************");
		logger.info(System.getProperty("line.separator"));

		logger.info("**********************************************************************************");
		logger.info("INÍCIO DA INDEXAÇÃO");

		indexar();

		logger.info("FIM DA INDEXAÇÃO");
		logger.info("**********************************************************************************");
		logger.info(System.getProperty("line.separator"));

		logger.info("**********************************************************************************");
		logger.info("INÍCIO DA BUSCA");

		buscar();

		logger.info("FIM DA BUSCA");
		logger.info("**********************************************************************************");
		logger.info(System.getProperty("line.separator"));

		testar(indexador);

		long fim = System.currentTimeMillis();
		logger.info("Tempo de execução: " + ((fim - inicio) / 1000) + "s");
	}

	public static void preparar() {
		preparador = new Preparador(new File(DIR_BASE_PREPARADA));
		preparador.prepararDiretorio(new File(DIR_BASE), "", EXTENSAO_ACEITA, null);
	}

	public static void indexar() {
		indexador = new Indexador(new File(DIR_INDICE));
		indexador.indexarDiretorio(new File(DIR_BASE_PREPARADA), EXTENSAO_ACEITA);
	}

	public static void buscar() {
		buscador = new Buscador(new File(DIR_INDICE));

		Path path = Paths.get(System.getProperty("user.home") + "/Desktop", "teste.txt");
		String dados = null;
		try {
			dados = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
			System.out.println("String do arquivo: " + dados);
		} catch (IOException e) {
			System.out.println("Não foi possível fazer a leitura do arquivo");
		}

		String parametro = dados;
		buscador.buscar(parametro);
	}

	public static void testar(Indexador indexador) {
		Experimento experimento = new Experimento(preparador, indexador, buscador);
		experimento.testar(new File(DIR_BASE), new File(DIR_BASE_PREPARADA), EXTENSAO_ACEITA);
	}

}
