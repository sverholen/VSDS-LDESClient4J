import { TreeNode } from "./tree-specification";

export class LdesFragmentRepository {
    private _fragments: any = {};

    public store(path: string, body: TreeNode) {
        this._fragments[path] = body;
    }

    public get(id: string) {
        return this._fragments[id];
    }

    public get fragments(): string[] {
        return Object.keys(this._fragments);
    }
}
