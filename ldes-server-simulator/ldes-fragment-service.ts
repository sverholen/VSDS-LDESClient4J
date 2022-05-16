import { TreeNode } from "./tree-specification";
import { readdir, readFile } from 'node:fs/promises';
import { LdesFragmentRepository } from "./ldes-fragment-repository";


export class LdesFragmentService {
    constructor(private baseUrl: URL, private repository: LdesFragmentRepository) { }

    private changeOrigin(url: URL, origin: URL): URL {
        url.protocol = origin.protocol;
        url.host = origin.host;
        return url;
    }

    public store(body: TreeNode): string {
        const fragmentUrl: URL = this.changeOrigin(new URL(body['@id']), this.baseUrl);
        body['@id'] = fragmentUrl.href;
        body['tree:relation'].forEach(x => x['tree:node'] = this.changeOrigin(new URL(x['tree:node']), this.baseUrl).href);

        const path = fragmentUrl.href.replace(this.baseUrl.href, '/');
        this.repository.store(path, body);
        return path;
    }

    public async seed(folderPath: string) {
        try {
            const files = await readdir(folderPath);
            for await (const file of files) {
                console.debug('found file: ', file);
                const content = await readFile(`${folderPath}/${file}`, { encoding: 'utf-8' });
                const fragment = this.store(JSON.parse(content));
                console.debug('seeded with: ', fragment);
            }
        } catch (err) {
            console.error(err);
        }
    }
}
