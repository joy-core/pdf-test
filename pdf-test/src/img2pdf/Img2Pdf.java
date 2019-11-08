package img2pdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class Img2Pdf {
	public static void main(String[] args) {
		File targetFile = new File("../pdf-test/pdfTarget/my_doc.pdf");
		File imgDir = new File("../pdf-test/imgSource");
		List<File> files = new ArrayList<>();
		if (imgDir.exists() && imgDir.isDirectory()) {
			File [] fileArr = imgDir.listFiles();
			for (File file : fileArr) {
				if (file.isFile()) {
					files.add(file);
				}
			}
		}
		if (files.size() == 0) {
			System.out.println("PDF create error, no images!");
			return;
		}
		createBlankPdf(targetFile, files.size());
		fillImg2Pdf(targetFile, files);
	}
	
	/**
	 * 创建空白PDF文档
	 * @param pageNum
	 */
	public static void createBlankPdf(File targetFile,int pageNum) {
		// 创建文档
		PDDocument pdDocument = new PDDocument();
		// 设置文档属性
		createMainInformation(pdDocument);
		for (int i=0; i<pageNum; i++) {
			// 创建页码
	        PDPage blankPage = new PDPage();
	        // 添加到文档
	        pdDocument.addPage(blankPage);
	      }
		try {
			// 保存文档
			pdDocument.save(targetFile);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭文档
				pdDocument.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("PDF created");
	}
	
	/**
	 * 设置文档属性
	 * @param pdDocument
	 */
	public static void createMainInformation(PDDocument pdDocument) {
		PDDocumentInformation pdd = pdDocument.getDocumentInformation();
		pdd.setAuthor("章三");
		pdd.setTitle("图片转换PDF");
		pdd.setCreator("PDF Examples");
		pdd.setSubject("主题哦");
		Calendar date = new GregorianCalendar();
		date.set(2019, 11, 6); 
		pdd.setCreationDate(date);
		date.set(2019, 11, 7); 
		pdd.setModificationDate(date);
		pdd.setKeywords("hi, my pdf!"); 
	}
	
	/**
	 * 填充图片到PDF文件
	 */
	public static void fillImg2Pdf (File targetFile, List<File> imgFileList) {
		// 加载现有文档
		PDDocument doc = null;
		try {
			doc = PDDocument.load(targetFile);
			for (int i = 0; i < imgFileList.size(); i++) {
				File imgFile = imgFileList.get(i);
				// 获取文档页面
				PDPage page = doc.getPage(i);
				PDImageXObject pdImage = PDImageXObject.createFromFile(imgFile.getPath(),doc);
				PDPageContentStream contents = new PDPageContentStream(doc, page);
				Map<String, Float> sizeMap = getSize(imgFile, page, 30, 30);
				contents.drawImage(pdImage, sizeMap.get("x"), sizeMap.get("y"), sizeMap.get("width") , sizeMap.get("height"));
				System.out.println("Image inserted");
				contents.close();
			}
			doc.save(targetFile);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (doc != null) {
				try {
					doc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 
	 * @param imgFile 图片文件
	 * @param page PDF页
	 * @param paddW 横向内边距
	 * @param paddH 纵向内边距
	 * @return
	 */
	private static Map<String, Float> getSize(File imgFile, PDPage page, float paddW, float paddH) {
		Map<String, Float> map = new HashMap<String, Float>();
		BufferedImage bufferedImage;
		try {
			bufferedImage = ImageIO.read(imgFile);
			float pageW = page.getBBox().getWidth() - paddW*2;// 文档真实宽
			float pageH = page.getBBox().getHeight() - paddH*2;// 文档真实高
			float imageW = bufferedImage.getWidth();
			float imageH = bufferedImage.getHeight();
			float scale = resize(bufferedImage, pageW, pageH);// 缩放比例
			// 获取位置
			// 文档横坐标
			map.put("x", paddW + (pageW - imageW*scale)/2);
			// 文档纵坐标
			map.put("y", pageH - imageH*scale + paddH);
			// 图片缩放后宽度
			map.put("width", imageW*scale);
			// 图片缩放后高度
			map.put("height", imageH*scale);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	/**
	 * 获取图片缩放比例
	 * @param imgFile
	 * @param pageW
	 * @param pageH
	 * @return
	 */
	private static float resize(File imgFile, float pageW, float pageH) {
		BufferedImage bufferedImage;
		try {
			bufferedImage = ImageIO.read(imgFile);
			float width = bufferedImage.getWidth();
			float height = bufferedImage.getHeight();
			float scale = 1f;
	        // 宽高都小于PDF，原样输出
	        if (width <= pageW && height <= pageH) {
				return scale;
			}
	        // 宽和高都大于PDF
	        if (width > pageW && height > pageH) {
				return pageW/width < pageH/height ? pageW/width : pageH/height;
			}
	        // 宽和高其一大于PDF
	        return width > pageW ? pageW/width : pageH/height;
		} catch (IOException e) {
			e.printStackTrace();
		}
        return 1;
	}
	
	/**
	 * 获取图片缩放比例
	 * @param bufferedImage
	 * @param pageW
	 * @param pageH
	 * @return
	 */
	private static float resize(BufferedImage bufferedImage, float pageW, float pageH) {
		float width = bufferedImage.getWidth();
		float height = bufferedImage.getHeight();
		float scale = 1f;
		// 宽高都小于PDF，原样输出
		if (width <= pageW && height <= pageH) {
			return scale;
		}
		// 宽和高都大于PDF
		if (width > pageW && height > pageH) {
			return pageW/width < pageH/height ? pageW/width : pageH/height;
		}
		// 宽和高其一大于PDF
		return width > pageW ? pageW/width : pageH/height;
	}
}
