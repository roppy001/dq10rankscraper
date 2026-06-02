# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DQ10 Rank Scraper は、ドラゴンクエストX（DQ10）の各種競技ランキングデータを `hiroba.dqx.jp` からスクレイピングし、AWS S3 にアップロードする Java アプリケーション。AWS Lambda 関数としてデプロイされる。

## Build & Run Commands

```bash
# ビルド（依存ライブラリを含むFat JARを生成）
./gradlew build

# JARのみ生成
./gradlew jar

# ローカル実行（引数: scraperKey, round）
java -cp build/libs/rankscraper-1.0.0-SNAPSHOT.jar roppy.dq10.rankanalytics.scraper.RankScraperMain slimerace 1

# 環境変数での実行
RANK_SCRAPER=slimerace RANK_ROUND=1 java -cp build/libs/rankscraper-1.0.0-SNAPSHOT.jar roppy.dq10.rankanalytics.scraper.RankScraperMain
```

## Architecture

### Data Flow

```
Lambda invocation (HandlerInput: scraperKey + round)
  → RankScraperMain.handleRequest()
  → RaceConfig lookup → instantiate Scraper implementation
  → Scraper.scrape() → JSoup HTML parsing of hiroba.dqx.jp
  → S3Uploader.compareLatest() → fetch latest TSV from S3
  → skip if identical, throw if empty, upload if different
  → write to S3: {sport}/{round}/{timestamp}.tsv
```

### Plugin Pattern (Scraper Implementations)

`Scraper.java` インターフェースを実装したクラスが競技ごとに存在する。`RaceConfig` enum が scraperKey → クラス名 のマッピングを管理し、`RankScraperMain` がリフレクションでインスタンス化する。

新しい競技を追加する手順:
1. `Scraper` インターフェースを実装したクラスを作成
2. `RaceConfig.java` に enum エントリを追加（scraperKey、クラス名、サブレース数、更新頻度を指定）

### Supported scraperKey values

| scraperKey | 競技 | サブレース数 | 更新頻度 |
|---|---|---|---|
| `slimerace` | スライムレース | 1 | 8時間ごと |
| `trinity` | バトルトリニティ | 6 | 8時間ごと |
| `casinoraid` | カジノレイド | 4 | 8時間ごと |
| `pencil` | バトルえんぴつ | 1 | 8時間ごと |
| `daifugo` | 大富豪段位戦 | 1 | 日次 |
| `daifugom` | 大富豪決定戦 | 1 | 8時間ごと |
| `fishing` | 釣り大会 | 2 | 8時間ごと |

### Key Classes

- **`RankScraperMain`** — Lambda `RequestHandler` の実装。`main()` でスタンドアロン起動も可能
- **`RaceConfig`** — 競技設定の enum レジストリ
- **`Scraper`** — スクレイパーインターフェース。戻り値は `List<List<RankItem>>`（外側がサブレース、内側がランキング行）
- **`S3Uploader`** — シングルトン。S3 との比較・アップロード処理。バケット名: `roppyracedata`
- **`UpdateTimeConfig`** — `EIGHT_HOURS` / `ONE_DAY` の2種。タイムスタンプ計算に使用

### Error Handling

スクレイピング結果が0件の場合は `ScrapingException` をスローして異常終了させる（メンテナンス等でデータが取れない場合に S3 を空データで上書きしないため）。

## Dependencies

- Java 8, Gradle 5.6.4
- `org.jsoup:jsoup:1.12.1` — HTML パース
- `com.amazonaws:aws-java-sdk` — Lambda / S3
- `org.projectlombok:lombok:1.16.10` — `@Data` / `@Getter` / `@AllArgsConstructor`
- `junit:junit:4.12` — テスト
