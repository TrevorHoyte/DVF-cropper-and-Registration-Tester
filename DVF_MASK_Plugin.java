import java.io.*;
import java.util.*;
import java.util.List;
import ij.*;
import ij.plugin.*;
import ij.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class DVF_MASK_Plugin implements PlugIn {

    public boolean littleEndian = false;

    public void run(String arg) {
        OpenDialog od = new OpenDialog("Select mhd file for deformation field ...", arg);
        String dir = od.getDirectory();
        String baseName = od.getFileName();
        if (baseName == null || baseName.length() == 0)
            return;
        int baseLength = baseName.length();
        String lowerBaseName = baseName.toLowerCase();
        boolean mhd = lowerBaseName.endsWith(".mhd");
        boolean mha = lowerBaseName.endsWith(".mha");
        boolean raw = lowerBaseName.endsWith(".raw");
        String headerName;
        if (mha || mhd) {
            headerName = baseName;
            baseName   = baseName.substring(0, baseLength - 4);
        }
        else {
            baseName   = baseName.substring(0, baseLength - 4);
            headerName = baseName + ".mhd";
        }
        IJ.showStatus("Opening " + headerName + "...");

       ImagePlus ipdvf = loadMotionfield(dir, baseName, headerName, mha);
    
		
	
		  
    
        //now loading Mask
        
        OpenDialog od1 = new OpenDialog("Select motion mask mha or mhd file ...", arg);
        String dir1 = od1.getDirectory();
        String baseName1 = od1.getFileName();
        if (baseName1 == null || baseName1.length() == 0)
            return;
         baseLength = baseName1.length();
        String lowerBaseName1 = baseName1.toLowerCase();
         mhd = lowerBaseName1.endsWith(".mhd");
         mha = lowerBaseName1.endsWith(".mha");
        raw = lowerBaseName1.endsWith(".raw");
        String headerName1;
        if (mha || mhd) {
            headerName1 = baseName1;
            baseName1   = baseName1.substring(0, baseLength - 4);
        }
        else {
            baseName1   = baseName1.substring(0, baseLength - 4);
            headerName1 = baseName1 + ".mhd";
        }
        IJ.showStatus("Opening " + headerName1 + "...");
        
        
        ImagePlus ipmask = loadMask(dir1, baseName1, headerName1, mha);
       
        IJ.showStatus(baseName1 + " opened");
        
        
       
        
        
        /// catch final image information
        ImagePlus mimp = multiplyandprocess (ipdvf,ipmask,dir);
   
        // Save Image
       
      //  FileInfo mfi = new FileInfo();
       // mfi = mimp.getFileInfo();
        
        
       //FileInfo fi = null;
	//File filetxt = filemhd(fi);
        
        
        
    }
    
    

    private ImagePlus loadMotionfield(String  dir,
                           String  baseName,
                           String  headerName,
                           boolean local) 
  
    {
    	
    	ImagePlus ipdvf=null;
    	
    	
    	File file = new File(dir+baseName+".mhd");
		File newfile = new File(dir + "processedDVF.mhd");
		Path path = Paths.get(dir + "processedDVF.mhd");

		BufferedReader reader;
		PrintWriter writer;
	String line;
    	
    	
    	
		 try{
                FileInfo fi = readHeader(dir, baseName, headerName, local);
                int widthxChannel= fi.width*3;
                
                if (fi.intelByteOrder )
                    IJ.run("Raw...", "open="+fi.directory+fi.fileName+" image=[32-bit Real] width="+widthxChannel+" height="+fi.height+" number="+fi.nImages+" little-endian");
                else
                	IJ.run("Raw...", "open="+fi.directory+fi.fileName+" image=[32-bit Real] width="+widthxChannel+" height="+fi.height+" number="+fi.nImages);
             

                IJ.run("Reslice [/]...", "output=1.000 start=Left avoid");
                IJ.run("Stack to Hyperstack...", "order=xyctz channels=3 slices="+fi.width+" frames=1 display=Grayscale");
          
                IJ.run("Reslice [/]...", "output=1.000 start=Top rotate avoid");
               // IJ.run( "Rename...","dvf");
                 ipdvf=IJ.getImage();
                ipdvf.setTitle("dvf");
                
              
            		if(newfile.createNewFile() || !newfile.createNewFile()) {
            			reader =new BufferedReader(new FileReader(file));
            			writer =new PrintWriter(new FileWriter(newfile, true));
            		
            		while ((line = reader.readLine()) != null) {
            			writer.println(line);
            		}
            		reader.close();
            		writer.close();
            		}
            	/////	
            	List<String> fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));

                for (int i = 0; i < fileContent.size(); i++) {
            		      if (fileContent.get(i).equals("ElementDataFile = "+baseName+ ".raw")) {
            		          fileContent.set(i, "ElementDataFile = processedDVF.raw");
            		         
            		          break;
            		       }
            		   }

            	   Files.write(path, fileContent, StandardCharsets.UTF_8);
            			
            	   
            	  List<String> fileContent1 = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));

                   for (int i = 0; i < fileContent1.size(); i++) {
               		      if (fileContent1.get(i).equals("BinaryDataByteOrderMSB = False")) {
                   		          fileContent1.set(i, "BinaryDataByteOrderMSB = True");
               		          break;
               		       }
               		   }

               	   Files.write(path, fileContent1, StandardCharsets.UTF_8);
            	
    	}
    	
    	
                catch (IOException e) {
                    IJ.error("MetaImage Reader: " + e.getMessage());
                }
                catch (NumberFormatException e) {
                    IJ.error("MetaImage Reader: " + e.getMessage());
                }
             return ipdvf;
             
                
    }
    
    private ImagePlus loadMask(String  dir1,
            String  baseName1,
            String  headerName1,
            boolean local) 
{
    	ImagePlus ipmask = null;

    	try{
			FileInfo fi = readHeader(dir1, baseName1, headerName1, local);
			
			IJ.open(""+fi.directory+fi.fileName+"");
			
			IJ.run("Duplicate...", "duplicate");
			IJ.run("Duplicate...", "duplicate");
			IJ.run("Concatenate...", "open image1="+fi.fileName+" image2="+fi.fileName+" image3="+fi.fileName+"");
			
			IJ.run("Re-order Hyperstack ...", "channels=[Frames (t)] slices=[Slices (z)] frames=[Channels (c)]");
			//IJ.run( "Rename...","mask");
		
			  ipmask=IJ.getImage();
             ipmask.setTitle("mask");
 
    	}
    	
			catch (IOException e) {
			IJ.error("MetaImage Reader: " + e.getMessage());
			}
			catch (NumberFormatException e) {
			IJ.error("MetaImage Reader: " + e.getMessage());
			}

       	return ipmask;
}
    
    
    		
    private ImagePlus multiplyandprocess (ImagePlus ipdvf,
    								ImagePlus ipmask,
    								String dir)
    {
    	ImagePlus mimp=null;
    	Path path = Paths.get(dir + "processedDVF.raw");
    	try {
    		   ImageCalculator ic = new ImageCalculator();
    		   ImagePlus imp3 = ic.run("Multiply create 32-bit stack", ipdvf,ipmask);
    		   imp3.show();
    		IJ.run("Reslice [/]...", "output=1.000 start=Left avoid");
    		IJ.run("Hyperstack to Stack");
    		IJ.run("Reslice [/]...", "output=1.000 start=Top rotate avoid");
    		mimp=IJ.getImage();
            mimp.setTitle("processedDVF");
             IJ.saveAs("Raw Data", path +"");
             IJ.showMessage("Your new DVF is saved as 'processedDVF'in the same directroy as the original");   
    	}
    	catch (NumberFormatException e) {
			IJ.error("MetaImage Reader: " + e.getMessage());
			}
   return mimp;
    
    }

  
    private FileInfo readHeader(String  dir,
                                String  baseName,
                                String  headerName,
                                 boolean local) throws IOException, NumberFormatException
    {
        FileInfo fi = new FileInfo();
        fi.directory  = dir;
        fi.fileFormat = FileInfo.RAW;

        Properties p = new Properties();
        ////for mha it is necessary to only pass the header to properties
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(dir + headerName)));
        String header= "";
        String line= "";
        do {
            line = in.readLine();
            header+= line + "\n";
        }while(line!= null && !line.startsWith("ElementDataFile"));
        //IJ.log("Header: " + header);
        long header_size= header.length();
        p.load(new ByteArrayInputStream(header.getBytes("ISO-8859-1"))); //load expects "ISO-8859-1" from a stream: http://docs.oracle.com/javase/7/docs/api/java/util/Properties.html#load%28java.io.InputStream%29

        String strObjectType = p.getProperty("ObjectType");
        String strNDims = p.getProperty("NDims");
        String strDimSize = p.getProperty("DimSize");
        String strElementSize = p.getProperty("ElementSize");
        String strElementDataFile = p.getProperty("ElementDataFile");
        String strElementByteOrderMSB = p.getProperty("ElementByteOrderMSB");
        if (null == strElementByteOrderMSB)
          strElementByteOrderMSB = p.getProperty("BinaryDataByteOrderMSB");
        String strElementNumberOfChannels = p.getProperty("ElementNumberOfChannels", "1");
        String strElementType = p.getProperty("ElementType", "MET_NONE");
        String strHeaderSize = p.getProperty("HeaderSize", "0");
	String strCompressedData = p.getProperty("CompressedData");

        if (strObjectType == null || !strObjectType.equalsIgnoreCase("Image"))
            throw new IOException("The specified file does not contain an image.");
        int ndims = Integer.parseInt(strNDims);
        if (strDimSize == null)
            throw new IOException("The image dimension size is unspecified.");
        else {
            String[] parts = strDimSize.split("\\s+");
            if (parts.length < ndims)
                throw new IOException("Invalid dimension size.");
            if (ndims > 1) {
                fi.width = Integer.parseInt(parts[0]);
                fi.height = Integer.parseInt(parts[1]);
                fi.nImages = 1;
                if (ndims > 2) {
                    for (int i = ndims - 1; i >= 2; --i)
                        fi.nImages *= Integer.parseInt(parts[i]);
                }
            }
            else {
                throw new IOException("Unsupported number of dimensions.");
            }
        }
        if (strElementSize != null) {
            String[] parts = strElementSize.split("\\s+");
            if (parts.length > 0)
                fi.pixelWidth  = Double.parseDouble(parts[0]);
            if (parts.length > 1)
                fi.pixelHeight = Double.parseDouble(parts[1]);
            if (parts.length > 2)
                fi.pixelDepth  = Double.parseDouble(parts[2]);
        }
        int numChannels = Integer.parseInt(strElementNumberOfChannels);
        if (numChannels == 1) {
            if (strElementType.equals("MET_UCHAR"))       { fi.fileType = FileInfo.GRAY8;           }
            else if (strElementType.equals("MET_SHORT"))  { fi.fileType = FileInfo.GRAY16_SIGNED;   }
            else if (strElementType.equals("MET_USHORT")) { fi.fileType = FileInfo.GRAY16_UNSIGNED; }
            else if (strElementType.equals("MET_INT"))    { fi.fileType = FileInfo.GRAY32_INT;      }
            else if (strElementType.equals("MET_UINT"))   { fi.fileType = FileInfo.GRAY32_UNSIGNED; }
            else if (strElementType.equals("MET_FLOAT"))  { fi.fileType = FileInfo.GRAY32_FLOAT;    }
            else if (strElementType.equals("MET_DOUBLE")) { fi.fileType = FileInfo.GRAY64_FLOAT;    }
            else {
                throw new IOException(
                    "Unsupported element type: " +
                    strElementType +
                    ".");
            }
        }
        else if (numChannels == 3) {
            if (strElementType.equals("MET_FLOAT")) fi.fileType= FileInfo.GRAY32_FLOAT;
            else {
                throw new IOException(
                    "Unsupported element type: " +
                    strElementType +
                    ".");
            }
        }
        else {
            throw new IOException("Unsupported number of channels.");
        }

        if (strElementDataFile != null && strElementDataFile.length() > 0) {
            if (strElementDataFile.equals("LOCAL")){
              fi.fileName = headerName;
                //IJ.log("Detected local data storage!" + fi.fileName);
                }
            else
              fi.fileName = strElementDataFile;
        }
        else {
            if (!local)
              fi.fileName = baseName + ".raw";
            else
              fi.fileName = headerName;
        }

        if (strElementByteOrderMSB != null) {
            if (strElementByteOrderMSB.length() > 0
                && (strElementByteOrderMSB.charAt(0) == 'T' ||
                    strElementByteOrderMSB.charAt(0) == 't' ||
                    strElementByteOrderMSB.charAt(0) == '1')) 
                fi.intelByteOrder = false;
            else
                fi.intelByteOrder = true;
        }
	//store compression info strCompressedData
        if (strCompressedData != null) {
            if (strCompressedData.length() > 0
                && (strCompressedData.charAt(0) == 'T' ||
                    strCompressedData.charAt(0) == 't' ||
                    strCompressedData.charAt(0) == '1')) 
                ////no FileInfo.ZLIB: http://rsbweb.nih.gov/ij/developer/api/constant-values.html#ij.io.FileInfo.ZIP
		fi.compression = FileInfo.COMPRESSION_UNKNOWN; //FileOpener.createInputStream will return null which will cause the ExtendedFileOpener to check for .zraw ;-)
       }

        fi.longOffset = (long)Integer.parseInt(strHeaderSize);
        if(local && fi.compression != FileInfo.COMPRESSION_UNKNOWN)
            fi.longOffset+= header_size;

        return fi;
        		
}
}
 