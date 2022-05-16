import fastify from 'fastify'
import minimist from 'minimist'
import { LdesFragmentRepository } from './ldes-fragment-repository';
import { LdesFragmentService } from './ldes-fragment-service';
import { TreeNode } from './tree-specification';

const server = fastify();
const args = minimist(process.argv.slice(2));
console.debug("arguments: ", args);

const baseUrl = new URL(args.baseUrl || 'http://localhost:8080')
const repository = new LdesFragmentRepository();
const service = new LdesFragmentService(baseUrl, repository);
const redirections: any = {};

server.get('/', async (_request, _reply) => {
  return { aliases: Object.keys(redirections), fragments: repository.fragments };
});

server.get('/*', async (request, reply) => {
  const fragmentId = request.url;
  const redirection = redirections[fragmentId];
  if (redirection !== undefined) {
    return reply.redirect(redirection);
  }

  const body = repository.get(fragmentId);
  reply.statusCode = body === undefined ? 404 : 200;
  return reply.send(body);
});

server.post('/ldes', async (request, reply) => {
  reply.statusCode = 201;
  return { path: service.store(request.body as TreeNode) };
});

interface Redirection {
  original: string;
  alias: string;
}

function withoutOrigin(path: string): string {
  const url = new URL(path);
  return path.replace(`${url.protocol}//${url.host}`, '');
}

server.post('/redirect', async (request, _reply) => {
  const redirection = request.body as Redirection;
  const original = withoutOrigin(redirection.original);
  const alias = withoutOrigin(redirection.alias);
  redirections[alias] = original;
  return { redirect: { from: alias, to: original } };
});

const options = { port: Number.parseInt(baseUrl.port), host: baseUrl.hostname };
server.listen(options, async (err, address) => {
  if (args.seed) {
    await service.seed(args.seed);
  }
  if (err) {
    console.error(err)
    process.exit(1)
  }
  console.log(`Server listening at ${address}`)
});
