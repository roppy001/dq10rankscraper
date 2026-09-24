package roppy.dq10.rankanalytics.scraper;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import roppy.dq10.rankanalytics.scraper.dto.RankItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class S3Uploader {
    private static final S3Uploader INSTANCE = new S3Uploader();
    private S3Uploader(){}
    public static S3Uploader getInstance(){return INSTANCE;}

    private static final String BUCKET_NAME = "roppyracedata";
    private static final int MAX_KEY_NUMBER = 200;

    public boolean compareLatest(List<List<RankItem>> targetListList, String prefix, int round) throws S3Exception{
        try {
            // 最新のデータを検索する

            String stringObjKeySearchKey = String.format("%s/%d/", prefix, round);

            S3Client s3Client = S3Client.builder()
                    .region(Region.AP_NORTHEAST_1)
                    .build();

            ListObjectsV2Request req = ListObjectsV2Request.builder()
                    .bucket(BUCKET_NAME)
                    .prefix(stringObjKeySearchKey)
                    .maxKeys(MAX_KEY_NUMBER)
                    .build();
            ListObjectsV2Response result;
            List<String> keyList = new ArrayList<>();

            do {
                result = s3Client.listObjectsV2(req);

                for (S3Object objectSummary : result.contents()) {
                    keyList.add(objectSummary.key());
                }

                String token = result.nextContinuationToken();
                req = req.toBuilder().continuationToken(token).build();
            } while (result.isTruncated());

            if(keyList.isEmpty()){
                return false;
            }

            Collections.sort(keyList);

            String latestKey = keyList.get(keyList.size() - 1);

            // 最新のデータを読み込む
            List<List<RankItem>> latestListList = new ArrayList<>();

            try(InputStream is = s3Client.getObject(GetObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(latestKey)
                        .build());
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr)){

                String line = br.readLine();

                if(line == null){
                    return false;
                }

                String [] firstTokens = line.split("\t",-1);

                if(firstTokens.length == 0){
                    return false;
                }
                int subRaceNumber;

                subRaceNumber = Integer.parseInt(firstTokens[0]);

                for(int i=0;i<subRaceNumber;i++){
                    latestListList.add(new ArrayList<>());
                }

                while((line = br.readLine()) != null){
                    String [] splittedLine = line.split("\t",-1);
                    if(splittedLine.length != 5) {
                        return false;
                    }

                    int subraceIndex = Integer.parseInt(splittedLine[0]);
                    if(subraceIndex < 0 || subraceIndex >= subRaceNumber){
                        return false;
                    }

                    RankItem item = new RankItem();
                    item.setRank(Integer.parseInt(splittedLine[1]));
                    item.setPoint(Integer.parseInt(splittedLine[2]));
                    item.setName(splittedLine[3]);
                    item.setExtraData(splittedLine[4]);

                    latestListList.get(subraceIndex).add(item);
                }

            }catch (NumberFormatException ioe) {
                return false;
            }catch (IOException ioe) {
                throw new S3Exception(ioe);
            }

            // 最新と新たにスクレイピングしたデータを比較
            if(targetListList.size() != latestListList.size()){
                return false;
            }

            for(int subraceIndex = 0;subraceIndex< targetListList.size(); subraceIndex++){
                List<RankItem> targetList = targetListList.get(subraceIndex);
                List<RankItem> latestList = latestListList.get(subraceIndex);

                if(targetList.size() != latestList.size()){
                    return false;
                }

                for(int i = 0 ; i< targetList.size();i++){
                    RankItem targetItem = targetList.get(i);
                    RankItem latestItem = latestList.get(i);

                    if(!targetItem.equals(latestItem)){
                        return false;
                    }
                }

            }


        } catch (SdkException e) {
            throw new S3Exception(e);
        }

        return true;
    }

    public void upload(List<List<RankItem>> listList, String prefix, int round,UpdateTimeConfig updateTimeConfig) throws S3Exception{
        try {
            //前回のランキング更新時刻を計算
            LocalDateTime updateTime = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));

            switch(updateTimeConfig){
                case EIGHT_HOURS:
                    updateTime = updateTime.plusHours(4);
                    updateTime =
                            LocalDateTime.of(updateTime.getYear(),
                                    updateTime.getMonth(),
                                    updateTime.getDayOfMonth(),
                                    (updateTime.getHour()/8)*8,
                                    0);
                    updateTime = updateTime.minusHours(4);
                    break;
                case ONE_DAY:
                    updateTime =
                            LocalDateTime.of(updateTime.getYear(),
                                    updateTime.getMonth(),
                                    updateTime.getDayOfMonth(),
                                    0,
                                    0);
                    break;
            }

            String timeString = updateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
            String stringObjKeyName = String.format("%s/%d/%s.tsv", prefix, round, timeString);

            StringBuilder sb = new StringBuilder();
            sb.append(listList.size() + "\t" + timeString + "\n");
            for(int subraceIndex=0 ;subraceIndex < listList.size();subraceIndex++ ) {
                for (RankItem item : listList.get(subraceIndex)) {
                    sb.append(subraceIndex + "\t" + item.getRank() + "\t" + item.getPoint() + "\t" + item.getName() + "\t" + item.getExtraData() + "\n");
                }
            }

            S3Client s3Client = S3Client.builder()
                    .region(Region.AP_NORTHEAST_1)
                    .build();

            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(stringObjKeyName)
                    .build(),
                RequestBody.fromString(sb.toString())
            );

        } catch (SdkException e) {
            throw new S3Exception(e);
        }
    }
}
