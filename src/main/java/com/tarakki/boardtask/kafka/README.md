# Apache Kafka 4.2.1 — Team Member Setup

## 1. Purpose

This document explains how a team member should install, configure, initialize, start, verify, and stop Kafka on their Windows machine before running any application that uses Kafka. It covers only Kafka infrastructure.

## 2. Required Environment

Each developer's machine should have:

- Operating System: Windows
- Java: JDK 21 recommended
- Kafka: Apache Kafka 4.2.1
- Kafka Mode: KRaft
- Controller Port: 9093
- Broker Port: 9092

Note: The Kafka installation directory can vary per machine. In this document we use `<KAFKA_HOME>` to mean the Kafka installation directory (for example `C:\technologies\kafka_2.13-4.2.1`).

## 3. What the Developer Needs to Run

Before starting the project, Kafka must have both a KRaft Controller and a Kafka Broker running.

Architecture:

Kafka

└─ Controller (localhost:9093)

└─ Broker (localhost:9092) → Topics

The application connects to `localhost:9092`. The controller uses `localhost:9093`.

## 4. First-Time Setup vs Daily Setup

First-time setup (do once):

1. Install Java
2. Install Kafka
3. Configure Controller
4. Configure Broker
5. Generate Cluster ID
6. Format Controller
7. Format Broker
8. Create required topics

Daily setup (every day you start development):

1. Start Controller
2. Start Broker
3. Verify Broker
4. Verify Topics
5. Run Project

Do NOT format Kafka daily; formatting is only for initial setup.

## 5. Install Java

Check Java:

```powershell
java -version
where java
```

Example output: `java version "21.x.x"`.

## 6. Install Kafka

Extract Kafka 4.2.1 to a suitable directory, e.g.:

```
C:\technologies\kafka_2.13-4.2.1
```

Directory contents should include `bin\windows`, `config`, `libs`, etc.

## 7. Define Kafka Home

`<KAFKA_HOME>` refers to your Kafka installation directory. Optionally set a Windows environment variable `KAFKA_HOME` pointing to that directory.

## 8. Kafka Configuration Files

You need two configuration files under `<KAFKA_HOME>\config`:

- `controller.properties`
- `broker.properties`

## 9. Controller Configuration (controller.properties)

Include the following important settings:

```
process.roles=controller
node.id=1
controller.quorum.bootstrap.servers=localhost:9093
listeners=CONTROLLER://:9093
advertised.listeners=CONTROLLER://localhost:9093
log.dirs=<KAFKA_HOME>/kraft-controller-logs
```

Important:
- Controller Node ID = 1
- Controller Port = 9093

## 10. Broker Configuration (broker.properties)

Include the following important settings:

```
process.roles=broker
node.id=2
controller.quorum.bootstrap.servers=localhost:9093
listeners=PLAINTEXT://localhost:9092
advertised.listeners=PLAINTEXT://localhost:9092
inter.broker.listener.name=PLAINTEXT
controller.listener.names=CONTROLLER
listener.security.protocol.map=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
log.dirs=<KAFKA_HOME>/kraft-broker-logs
```

Important:
- Broker Node ID = 2
- Broker Port = 9092

For local development it is OK to disable the log cleaner:

```
log.cleaner.enable=false
```

## 11. Separate Storage Directories

Controller and broker must use separate directories, for example:

```
<KAFKA_HOME>\kraft-controller-logs
<KAFKA_HOME>\kraft-broker-logs
```

Do not configure both processes to use the same directory.

## 12. Generate Cluster ID (first-time only)

Open a terminal and run from the Kafka directory:

```powershell
cd /d <KAFKA_HOME>
bin\windows\kafka-storage.bat random-uuid
```

Save the printed UUID as `<KAFKA_CLUSTER_ID>`.

## 13. Format Controller Storage (first-time only)

```powershell
cd /d <KAFKA_HOME>
bin\windows\kafka-storage.bat format ^
  --cluster-id <KAFKA_CLUSTER_ID> ^
  --standalone ^
  --config config\controller.properties
```

After success, verify `meta.properties` exists in the controller log directory:

```powershell
dir <KAFKA_HOME>\kraft-controller-logs
type <KAFKA_HOME>\kraft-controller-logs\meta.properties
```

## 14. Format Broker Storage (first-time only)

```powershell
cd /d <KAFKA_HOME>
bin\windows\kafka-storage.bat format ^
  --cluster-id <KAFKA_CLUSTER_ID> ^
  --no-initial-controllers ^
  --config config\broker.properties
```

Verify `meta.properties` in the broker log directory and that it shows `node.id=2`.

## 15. Important: Do Not Format Kafka Again

Formatting is only for initial setup. Re-formatting can reset or corrupt local Kafka data.

## 16. Create Topics (first-time only)

After Kafka is running, create required topics, for example `audit-logging-topic`:

```powershell
bin\windows\kafka-topics.bat ^
  --bootstrap-server localhost:9092 ^
  --create ^
  --topic audit-logging-topic

bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list
```

## 17. Daily Kafka Startup

Start the Controller in one terminal:

```powershell
cd /d <KAFKA_HOME>
bin\windows\kafka-server-start.bat config\controller.properties
```

Start the Broker in another terminal:

```powershell
cd /d <KAFKA_HOME>
bin\windows\kafka-server-start.bat config\broker.properties
```

## 18. Verify Controller and Broker

Check ports:

```powershell
netstat -ano | findstr 9093
netstat -ano | findstr 9092
```

Expected: `LISTENING` on both ports.

## 19. Verify Topics

```powershell
cd /d <KAFKA_HOME>
bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list
```

Confirm required topics such as `audit-logging-topic` exist.

## 20. PowerShell Notes

When using PowerShell, prefix batch files with `.\