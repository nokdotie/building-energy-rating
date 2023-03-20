# Contributing

Bellow is a small guide to help you setup your local environment.

## Google Cloud Platform
This application uses Google Cloud Platform, also known as GCP. [Firestore](https://cloud.google.com/firestore) is this project's main datastore. It requires your local environment to have GCP credentials. The easiest way to do this is with the [Google Cloud Command Line Interface](https://cloud.google.com/cli).

To install the tool, just follow [Google’s installation guide](https://cloud.google.com/sdk/docs/install).

Once installed, you can authenticate by initializing the tool.
```sh
gcloud init
```

When asked, select `deed-io` as the “cloud project to use”. If you missed that step, the project can be configured afterwards with a CLI command.
```sh
gcloud config set project deed-io
```

With the tool properly configured, you should be able to credentials for local application development.
```sh
gcloud auth application-default login
```

## Production
This application contains two environments: production and other. To run the application in the production environment, you must set the `ENV` environment variable.

```sh
ENV=production sbt api/run
```

Beware, the production environment should be kept for production use.
