package camerapreview.opencv.hig.opencvcamerapreview;

/**
 * Created by mark.labios on 6/30/2016.
 */
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;

public class Mini {
    public static String answer = "";

    public Mini(){

      /*  int ret;
        ret = compareFeature(img1, img2);
        double s = ret/500.00;
        if (true) {
            System.out.println("MOSBY: Two images are same. "+s);
            answer="Similarity detected "+s*100+"%";
        } else {
            System.out.println("MOSBY: Two images are different.");
            answer="No similarity detected";
        }*/
    }

    public boolean compare(Mat img1,Mat img2){
        int ret;
        ret = compareFeature(img1, img2);
        if(ret>0) {
            answer = "Similarity detected" ;
            return true;
        }
        else{
            answer="No similarity detected";
            return false;
        }
    }

    public static int compareFeature(Mat img1, Mat img2) {
        int retVal = 0;
        long startTime = System.currentTimeMillis();

//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Load images to compare
     //   Mat img1 = Imgcodecs.imread(filename1, Imgcodecs.CV_LOAD_IMAGE_COLOR);
     //   Mat img2 = Imgcodecs.imread(filename2, Imgcodecs.CV_LOAD_IMAGE_COLOR);

        // Declare key point of images
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();

        // Definition of ORB key point detector and descriptor extractors
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

        // Detect key points
        detector.detect(img1, keypoints1);
        detector.detect(img2, keypoints2);

        // Extract descriptors
        extractor.compute(img1, keypoints1, descriptors1);
        extractor.compute(img2, keypoints2, descriptors2);

        // Definition of descriptor matcher
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        // Match points of two images
        MatOfDMatch matches = new MatOfDMatch();
//  System.out.println("Type of Image1= " + descriptors1.type() + ", Type of Image2= " + descriptors2.type());
//  System.out.println("Cols of Image1= " + descriptors1.cols() + ", Cols of Image2= " + descriptors2.cols());

        // Avoid to assertion failed
        // Assertion failed (type == src2.type() && src1.cols == src2.cols && (type == CV_32F || type == CV_8U)
        if (descriptors2.cols() == descriptors1.cols()) {
            matcher.match(descriptors1, descriptors2 ,matches);

            // Check matches of key points
            DMatch[] match = matches.toArray();
            double max_dist = 0; double min_dist = 100;

            for (int i = 0; i < descriptors1.rows(); i++) {
                double dist = match[i].distance;
                if( dist < min_dist ) min_dist = dist;
                if( dist > max_dist ) max_dist = dist;
            }
            System.out.println(" MOSBY: max_dist=" + max_dist + ", min_dist=" + min_dist);

            // Extract good images (distances are under 10)

            for (int i = 0; i < descriptors1.rows(); i++) {
                if (match[i].distance <= 10) {
                    retVal++;
                }
            }
            System.out.println("MOSBY: matching count=" + retVal +" descriptors rows="+descriptors1.rows());
        }

        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("MOSBY: estimatedTime=" + estimatedTime + "ms");

        return retVal;
    }

    public static String getAnswer(){
        return answer;
    }


}

