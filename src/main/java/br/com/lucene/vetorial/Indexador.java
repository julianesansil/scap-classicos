package br.com.lucene.vetorial;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.Tika;

import br.com.lucene.Util;

public class Indexador {
	private static Logger logger = Logger.getLogger(Indexador.class);

	// Diret�rio de armazenamento do �ndice
	private File dirIndice;

	// Representa o diret�rio do �ndice em mem�ria
	private Directory dirIndiceEmMemoria;

	// Respons�vel pelo pr�-processamento do texto
	private Analyzer analisador;

	// Configura a cria��o do �ndice
	private IndexWriterConfig config;

	// Biblioteca que cria e mant�m o �ndice
	private IndexWriter writer;

	// Biblioteca que extrai texto de diversos formatos conhecidos
	private Tika tika;

	public Indexador(File dirIndice) {
		this.dirIndice = dirIndice;
		this.configurarIndexador();
	}

	public Tika getTika() {
		if (tika == null) {
			tika = new Tika();
		}
		return tika;
	}

	public void configurarIndexador() {
		try {
			Util.esvaziarDiretorio(dirIndice);
			logger.info("Diretorio do �ndice: " + dirIndice.getAbsolutePath());
			dirIndiceEmMemoria = new SimpleFSDirectory(dirIndice);

			// A separa��o dos termos � feita atrav�s dos espa�os em branco do texto
			analisador = new WhitespaceAnalyzer(Version.LUCENE_48);

			config = new IndexWriterConfig(Version.LUCENE_48, analisador);
			// Inicializa o IndexWriter para grava��o
			writer = new IndexWriter(dirIndiceEmMemoria, config);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void indexarDiretorio(File dirBasePreparada, String sufixoAceito) {
		for (File arquivo : Util.listarArquivosDiretorio(dirBasePreparada, "", sufixoAceito)) {
			if (arquivo.isFile())
				indexarArquivo(arquivo);
			else
				indexarDiretorio(arquivo, sufixoAceito);
		}

		try {
			writer.commit();
//			writer.close();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private void indexarArquivo(File arquivo) {
		StringBuffer msg = new StringBuffer();
		msg.append("Indexando o arquivo: ");
		msg.append(arquivo.getAbsoluteFile());
		msg.append(", ");
		msg.append(arquivo.length() / 1000);
		msg.append("kb");
		logger.info(msg);

		try {
			// Extrai o conte�do do arquivo com o Tika
			String textoExtraido = getTika().parseToString(arquivo);

			// Monta um Document para indexa��o
			// Cada item indexado � um Document e cont�m uma cole��o de campos
			Document documento = new Document();
			documento.add(new TextField("Caminho", arquivo.getAbsolutePath(), Store.YES));
			// Field.Store.YES: armazena uma c�pia do texto no �ndice
			documento.add(new TextField("Texto", textoExtraido, Store.YES));

			// Adiciona o Document no �ndice
			// Este s� estar� dispon�vel para consulta ap�s o commit
			writer.addDocument(documento);

		} catch (Exception e) {
			logger.error(e);
		}
	}

}
