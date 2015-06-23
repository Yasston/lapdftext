# Installation Instructions #

The current version of LA-PDFText requires Java 1.6 in order to run and supports the following operating systems:
  1. MacOSX (Developed on Mountain Lion, but should work in most environments).
  1. Windows XP
  1. Linux

Mac and windows users can run their installers by simply double-clicking the installer icon while Linux users are provided with a **.tar.gz archive.**

_Note: OS X 10.8 (Mountain Lion) introduced a new security feature that, by default, limits "acceptable" applications to only those downloaded from the Mac App store. Thankfully, you can alter this in the system preferences. Go to "Security & Privacy" and change the "Allow applications downloaded from:" to "Anywhere". LA-PDFText will launch successfully after this change._

Usage of the tool as a software application is currently based on a set of command-line functions.

  * `blockify` - obtain XML-data for each block and no classification.
  * `blockifyClassify` - obtain XML for each block and classification based on a rule file.
  * `blockStatistics` - obtain statistics for each block based on spatial characteristics.
  * `imagifyBlocks` - draw out the blocks into PNG images for debugging
  * `imagifySections` - draw out the blocks with labels based on sections for debugging
  * `extractFullText` - simply extract the text from the PDF based on a list of section headings (designated in the rule file)

Running each command without parameters brings up usage instructions.

Each command can be run with only one parameter: either a full path to a PDF file or a directory containing PDFs. This invokes the output directory as the same directory where the PDF files are found and the rule file to be a simple baseline default based on a two-column format. This works quite well with most scientific papers.

The final functionality is experimental and is still under development. This is a 'directory watching function' that runs a process that 'watches' a directory and runs any one of the other six listed commands above when a new PDF file is added to the directory (generating output in another designated directory). When a pdf file is removed from the watched directory, the derived data (images, text, etc.) are removed as well.

The development of rule files will be covered in more detail in another page.