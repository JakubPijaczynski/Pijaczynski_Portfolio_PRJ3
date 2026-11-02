import {BackendApplication} from '../..';
import {
  createRestAppClient,
  givenHttpServerConfig,
  Client,
} from '@loopback/testlab';

export async function setupApplication(): Promise<AppWithClient> {
  const restConfig = givenHttpServerConfig({
    // Customize the server configuration here.
    // Empty values (undefined, '') will be ignored by the helper.
    //
    // host: process.env.HOST,
    // port: +process.env.PORT,
  });

  const app = new BackendApplication({
    rest: restConfig,
  });

  // Use a pure in-memory database in tests (no db.json file)
  app
    .bind('datasources.config.memory')
    .to({name: 'memory', connector: 'memory'});

  await app.boot();
  await app.start();

  const client = createRestAppClient(app);

  return {app, client};
}

export interface AppWithClient {
  app: BackendApplication;
  client: Client;
}
