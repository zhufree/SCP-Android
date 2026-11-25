# Synthetic Spider release fixture

Generated offline with SCP-Spider commit `a1eeadb`. The work database used `migrate_work` and two synthetic type-1 catalog entries. `/scp-001` had the body `<div>Synthetic body</div>` and tag `safe`. `/scp-2000` was marked removed, leaving a valid zero-row snapshot for its range.

`export_release` used `now=1795521600`, a parts directory, and `full_detail=True`. `parts_merge` generated `merged.db`. The manifest and its three assets came from `release_pack` with tag `db-20261124-1`, run ID `101`, and the same timestamp. `release_verify` passed before these four files were copied together. `full.db.gz` and `merged.db.gz` are compressed copies of the two standalone exports.
